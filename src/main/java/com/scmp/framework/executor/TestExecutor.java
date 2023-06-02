package com.scmp.framework.executor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.scmp.framework.testng.listeners.SuiteListener;
import com.scmp.framework.testng.listeners.InvokedMethodListener;
import com.scmp.framework.testng.listeners.AnnotationTransformerListener;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlSuite.ParallelMode;
import org.testng.xml.XmlTest;

import com.scmp.framework.context.RunTimeContext;
import com.scmp.framework.utils.Figlet;

import static com.scmp.framework.utils.Constants.*;

@Component
public class TestExecutor {
	private final RunTimeContext context;
	private final List<String> packageList = new ArrayList<>();
	private static final Logger frameworkLogger = LoggerFactory.getLogger(TestExecutor.class);

	@Autowired
	public TestExecutor(RunTimeContext context) {

		this.context = context;

		if (this.context.isLocalExecutionMode()) {
			prepareWebDriver();
		}
	}

	/**
	 * Prepare Web driver for testing browser
	 * e.g. download web driver, set environment variables
	 */
	private void prepareWebDriver() {
		File directory = new File(context.getFrameworkConfigs().getDriverHome());
		if (!directory.exists()) {
			directory.mkdir();
		}

		System.setProperty(WDM_CACHE_PATH, context.getFrameworkConfigs().getDriverHome());
		WebDriverManager.chromedriver().setup();
		context.setGlobalVariables(
				CHROME_DRIVER_PATH, WebDriverManager.chromedriver().getDownloadedDriverPath());

		WebDriverManager.firefoxdriver().setup();
		context.setGlobalVariables(
				FIREFOX_DRIVER_PATH, WebDriverManager.firefoxdriver().getDownloadedDriverPath());
	}

	/**
	 * Run tests under specific packages and defined test classes
	 *
	 * @param packages Package list
	 * @return userDefinedTestClasses status, passed / failed
	 * @throws Exception exception
	 */
	public boolean runTests(List<String> packages) throws Exception {
		System.out.println("***************************************************");
		this.packageList.addAll(packages);

		URL testPackagesUrl;
		List<URL> testPackagesUrls = new ArrayList<>();
		String testClassPackagePath =
				"file:" + TARGET_PATH + File.separator + "test-classes" + File.separator;

		// Add URL for each userDefinedTestClasses package
		for (String packageName : packageList) {
			testPackagesUrl = new URL(testClassPackagePath + packageName.replaceAll("\\.", "/"));
			testPackagesUrls.add(testPackagesUrl);
		}

		// Find test class by annotation: org.testng.annotations.Test.class
		Reflections reflections =
				new Reflections(new ConfigurationBuilder().setUrls(testPackagesUrls).setScanners(new MethodAnnotationsScanner()));
		Set<Method> testNGTests = reflections.getMethodsAnnotatedWith(org.testng.annotations.Test.class);

		Map<String, List<Method>> methods = createTestsMap(testNGTests);

		ExecutorService executor = Executors.newCachedThreadPool();
		List<FutureTask<Boolean>> list = new ArrayList<>();

		// Available browser types
		// Chrome, Firefox, Random (if random, either chrome or firefox will be assigned)
		String[] browsers = context.getFrameworkConfigs().getBrowserType().split(",");
		for (String browser : browsers) {
			XmlSuite suite = constructXmlSuite(browser, methods);
			String suiteFile = writeTestNGFile(suite, "testsuite" + "-" + browser);

			FutureTask<Boolean> futureTask = new FutureTask<>(new TestExecutorService(suiteFile));
			list.add(futureTask);

			executor.submit(futureTask);
		}

		// Wait for the userDefinedTestClasses completion
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

	/**
	 * Create the xml testng suite for execution
	 *
	 * @param browser browser name
	 * @param methods test methods
	 * @return XML suite
	 */
	public XmlSuite constructXmlSuite(String browser, Map<String, List<Method>> methods) {
		ArrayList<String> listeners = new ArrayList<>();
		ArrayList<String> groupsInclude = new ArrayList<>();
		ArrayList<String> groupsExclude = new ArrayList<>();

		// Add groups
		Collections.addAll(
				groupsInclude,
				context.getFrameworkConfigs().getIncludeGroups().split("\\s*,\\s*"));
		Collections.addAll(
				groupsExclude,
				context.getFrameworkConfigs().getExcludeGroups().split("\\s*,\\s*"));

		// Initialize XML Suite
		XmlSuite suite = new XmlSuite();
		suite.setName("Test Suite");
		suite.setPreserveOrder(true);

		/*
		 *  Set parallel mode to METHODS level
		 *  Each method will be taken care of by 1 thread
		 */
		suite.setThreadCount(context.getFrameworkConfigs().getThreadCount());
		suite.setDataProviderThreadCount(context.getFrameworkConfigs().getDataProviderThreadCount());
		suite.setParallel(ParallelMode.METHODS);
		// Additional output, including test class and method names.
		suite.setVerbose(2);

		// Add listeners
		listeners.add(SuiteListener.class.getName());
		listeners.add(InvokedMethodListener.class.getName());
		listeners.add(AnnotationTransformerListener.class.getName());
		suite.setListeners(listeners);

		// Initialize the XML Test Suite
		XmlTest test = new XmlTest(suite);
		test.setName("Automated Test");
		test.addParameter("browser", browser);
		test.setIncludedGroups(groupsInclude);
		test.setExcludedGroups(groupsExclude);

		// Add test class and methods
		test.setXmlClasses(createXmlClasses(methods));

		return suite;
	}

	/**
	 * Create TestNG XML Class
	 * (not handling methods, methods will be controlled by includes and excludes rules)
	 *
	 * @param methods all available test methods
	 * @return TestNG XML class list
	 */
	public List<XmlClass> createXmlClasses(Map<String, List<Method>> methods) {

		return methods.keySet().stream().map(className -> {
			if (!className.contains("TestRunner")) {
				return new XmlClass(className);
			}
			return null;
		}).filter(Objects::nonNull).collect(Collectors.toList());
	}

	/**
	 * Write the XML suite to target folder
	 *
	 * @param suite    XML suite
	 * @param fileName file name to be created
	 * @return full file path of the xml file
	 */
	private String writeTestNGFile(XmlSuite suite, String fileName) {
		// Print out Suite XML
		System.out.println(suite.toXml());
		String suiteXML = System.getProperty("user.dir") + "/target/" + fileName + ".xml";

		try {
			FileWriter writer = new FileWriter(suiteXML);
			writer.write(suite.toXml());
			writer.flush();
			writer.close();
		} catch (IOException e) {
			frameworkLogger.error("Ops!", e);
		}

		return suiteXML;
	}

	/**
	 * Create a test class, test methods mapping
	 *
	 * @param methods all testng tests
	 * @return test class, test method map
	 */
	public Map<String, List<Method>> createTestsMap(Set<Method> methods) {
		Map<String, List<Method>> testsMap = new HashMap<>();
		methods.forEach(
				method -> {
					// Get method list from specific test class
					String className =
							method.getDeclaringClass().getPackage().getName()
									+ "."
									+ method.getDeclaringClass().getSimpleName();

					// If the method list is empty, initialize it and add it to test class map
					List<Method> methodsList =
							testsMap.computeIfAbsent(className, k -> new ArrayList<>());
					// Add method to list
					methodsList.add(method);
				});

		return testsMap;
	}
}

class TestExecutorService implements Callable<Boolean> {
	private final String suite;

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
