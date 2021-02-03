package com.scmp.framework.executor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import com.scmp.framework.testng.listeners.SuiteListener;
import com.scmp.framework.utils.ConfigFileKeys;
import com.scmp.framework.testng.listeners.InvokedMethodListener;
import com.scmp.framework.testng.listeners.RetryListener;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlSuite.ParallelMode;
import org.testng.xml.XmlTest;

import com.scmp.framework.context.RunTimeContext;
import com.scmp.framework.utils.Figlet;

import static com.scmp.framework.utils.Constants.*;

public class TestExecutor {
  private final RunTimeContext context;
  private ArrayList<String> items = new ArrayList<>();

  private static final Logger frameworkLogger = LoggerFactory.getLogger(TestExecutor.class);

  public TestExecutor() {

    context = RunTimeContext.getInstance();

    if(RunTimeContext.getInstance().isLocalExecutionMode()) {
      prepareWebDriver();
    }
  }

  private void prepareWebDriver() {
    System.setProperty(WDM_CACHE_PATH, context.getProperty(ConfigFileKeys.DRIVER_HOME));
    WebDriverManager.chromedriver().setup();
    context.setGlobalVariables(
            CHROME_DRIVER_PATH, WebDriverManager.chromedriver().getDownloadedDriverPath());

    WebDriverManager.firefoxdriver().setup();
    context.setGlobalVariables(
            FIREFOX_DRIVER_PATH, WebDriverManager.firefoxdriver().getDownloadedDriverPath());
  }

  public boolean runner(String pack, List<String> tests) throws Exception {
    return triggerTest(pack, tests);
  }

  public boolean runner(String pack) throws Exception {
    return runner(pack, new ArrayList<>());
  }

  public boolean triggerTest(String pack, List<String> tests) throws Exception {
    System.out.println("***************************************************");

    boolean hasFailures = false;
    if (context.getProperty(ConfigFileKeys.FRAMEWORK).equalsIgnoreCase("testng")) {
      hasFailures = this.execute(tests, pack);
    }

    return hasFailures;
  }

  /**
   * @param test test classes
   * @param pack Package list, separate by comma, e.g. com.test.package1, com.test.package2
   * @return
   * @throws Exception
   */
  public boolean execute(List<String> test, String pack) throws Exception {
    URL testClassUrl;
    List<URL> testClassUrls = new ArrayList<>();
    String testClassPackagePath =
        "file:"
            + TARGET_PATH
            + File.separator
            + "test-classes"
            + File.separator;

    // Add test packages to item list
    Collections.addAll(items, pack.split("\\s*,\\s*"));

    // Add URL for each test package
    for (int i = 0; i < items.size(); i++) {
      testClassUrl = new URL(testClassPackagePath + items.get(i).replaceAll("\\.", "/"));
      testClassUrls.add(testClassUrl);
    }

    // Find test class by annotation: org.testng.annotations.Test.class
    Reflections reflections =
        new Reflections(
            new ConfigurationBuilder()
                .setUrls(testClassUrls)
                .setScanners(new MethodAnnotationsScanner()));
    Set<Method> resources = reflections.getMethodsAnnotatedWith(org.testng.annotations.Test.class);

    Map<String, List<Method>> methods = createTestsMap(resources);

    ExecutorService executor = Executors.newCachedThreadPool();
    List<FutureTask<Boolean>> list = new ArrayList<>();

    // Available browser types
    // Chrome, Firefox, Random (if random, either chrome or firefox will be assigned)
    String[] browsers =
        RunTimeContext.getInstance().getProperty(ConfigFileKeys.BROWSER_TYPE).split(",");
    for (String browser : browsers) {
      XmlSuite suite = constructXmlSuite(browser, test, methods);
      String suiteFile = writeTestNGFile(suite, "testsuite" + "-" + browser);

      FutureTask<Boolean> futureTask = new FutureTask<>(new TestExecutorService(suiteFile));
      list.add(futureTask);

      executor.submit(futureTask);
    }

    // Wait for the test completion
    while (true) {
      boolean isDone = true;
      for (FutureTask<Boolean> futureTask : list) {
        isDone = isDone && futureTask.isDone();
      }

      if (isDone) {
        // Shutdown executor service
        executor.shutdown();
        break;
      } else {
        TimeUnit.SECONDS.sleep(1);
      }
    }

    boolean hasFailure = false;
    //  Get the result
    for (FutureTask<Boolean> result : list) {
      hasFailure = hasFailure || result.get();
    }

    Figlet.print("Test Completed");

    return hasFailure;
  }

  public XmlSuite constructXmlSuite(
      String browser, List<String> tests, Map<String, List<Method>> methods) {
    ArrayList<String> listeners = new ArrayList<>();
    ArrayList<String> groupsInclude = new ArrayList<>();
    ArrayList<String> groupsExclude = new ArrayList<>();

    // Add groups
    Collections.addAll(
        groupsInclude, context.getProperty(ConfigFileKeys.INCLUDE_GROUPS).split("\\s*,\\s*"));
    Collections.addAll(
        groupsExclude, context.getProperty(ConfigFileKeys.EXCLUDE_GROUPS).split("\\s*,\\s*"));

    // Initialize XML Suite
    XmlSuite suite = new XmlSuite();
    suite.setName("Test Suite");
    suite.setPreserveOrder(true);

    /*
     *  Set parallel mode to METHODS level
     *  Each method will be taken care of by 1 thread
     */
    suite.setThreadCount(Integer.parseInt(context.getProperty(ConfigFileKeys.THREAD_COUNT)));
    suite.setDataProviderThreadCount(
        Integer.parseInt(context.getProperty(ConfigFileKeys.DATAPROVIDER_THREAD_COUNT)));
    suite.setParallel(ParallelMode.METHODS);
    suite.setVerbose(2);

    // Add listeners
    listeners.add(SuiteListener.class.getName());
    listeners.add(InvokedMethodListener.class.getName());
    listeners.add(RetryListener.class.getName());
    suite.setListeners(listeners);

    // Initialize the XML Test Suite
    XmlTest test = new XmlTest(suite);
    test.setName("Web Test");
    test.addParameter("browser", browser);
    test.setIncludedGroups(groupsInclude);
    test.setExcludedGroups(groupsExclude);

    // Add test class and methods
    List<XmlClass> xmlClasses = new ArrayList<>();
    writeXmlClass(tests, methods, xmlClasses);
    test.setXmlClasses(xmlClasses);

    return suite;
  }

  public void writeXmlClass(
      List<String> testcases, Map<String, List<Method>> methods, List<XmlClass> xmlClasses) {
    for (String className : methods.keySet()) {
      if (className.contains("Test")) {
        if (testcases.size() == 0) {
          xmlClasses.add(createClass(className, methods.get(className)));
        } else {
          for (String s : testcases) {
            for (int j = 0; j < items.size(); j++) {
              String testName = items.get(j).concat("." + s);
              if (testName.equals(className)) {
                xmlClasses.add(createClass(className, methods.get(className)));
              }
            }
          }
        }
      }
    }
  }

  private String writeTestNGFile(XmlSuite suite, String fileName) {
    // Print out Suite XML
    System.out.println(suite.toXml());
    String suiteXML = System.getProperty("user.dir") + "/target/" + fileName + ".xml";

    try {
      FileWriter writer = new FileWriter(new File(suiteXML));
      writer.write(suite.toXml());
      writer.flush();
      writer.close();
    } catch (IOException e) {
      frameworkLogger.error("Ops!", e);
    }

    return suiteXML;
  }

  private XmlClass createClass(String className, List<Method> methods) {
    XmlClass clazz = new XmlClass();
    clazz.setName(className);
    return clazz;
  }

  public Map<String, List<Method>> createTestsMap(Set<Method> methods) {
    Map<String, List<Method>> testsMap = new HashMap<>();
    methods.stream()
        .forEach(
            method -> {
              // Get method list from specific test class
              List<Method> methodsList =
                  testsMap.get(
                      method.getDeclaringClass().getPackage().getName()
                          + "."
                          + method.getDeclaringClass().getSimpleName());

              // If the method list is empty, initialize it and add it to test class map
              if (methodsList == null) {
                methodsList = new ArrayList<>();
                testsMap.put(
                    method.getDeclaringClass().getPackage().getName()
                        + "."
                        + method.getDeclaringClass().getSimpleName(),
                    methodsList);
              }

              // Add method to list
              methodsList.add(method);
            });

    return testsMap;
  }
}

class TestExecutorService implements Callable<Boolean> {
  private String suite;

  public TestExecutorService(String file) {
    suite = file;
  }

  @Override
  public Boolean call() {
    List<String> suiteFiles = new ArrayList<>();
    suiteFiles.add(suite);

    TestNG testNG = new TestNG();
    testNG.setTestSuites(suiteFiles);
    testNG.run();

    return testNG.hasFailure();
  }
}
