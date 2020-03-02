package com.rayzhou.framework.executor;

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

import com.rayzhou.framework.testng.listeners.InvokedMethodListener;
import com.rayzhou.framework.testng.listeners.RetryListener;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlSuite.ParallelMode;
import org.testng.xml.XmlTest;

import com.rayzhou.framework.context.RunTimeContext;
import com.rayzhou.framework.utils.Figlet;
import com.rayzhou.framework.utils.PackageUtil;


public class TestExecutor 
{
	private final RunTimeContext context;
	private ArrayList<String> items = new ArrayList<String>();
	private List<Class> testcases;

	public TestExecutor() throws IOException
	{
		context = RunTimeContext.getInstance();
	}

	public boolean runner(String pack, List<String> tests) throws Exception 
	{
		return triggerTest(pack, tests);
	}

	public boolean runner(String pack) throws Exception 
	{
		return runner(pack, new ArrayList<String>());
	}

	public boolean triggerTest(String pack, List<String> tests) throws Exception 
	{
		System.out.println("***************************************************");

		testcases = new ArrayList<Class>();

		boolean hasFailures = false;
		if (context.getProperty("FRAMEWORK").equalsIgnoreCase("testng")) 
		{
			// Get all class with name contains "Test"
			PackageUtil.getClasses(pack).stream().forEach(s -> 
			{
				if (s.toString().contains("Test")) 
				{
					testcases.add((Class) s);
				}
			});

			hasFailures = this.execute(tests, pack);
		}

		return hasFailures;
	}

	/**
	 * 
	 * @param test test classes
	 * @param pack Package list, separate by comma, e.g. com.test.package1, com.test.package2
	 * @return
	 * @throws Exception
	 */
	public boolean execute(List<String> test, String pack) throws Exception 
	{
		URL testClassUrl = null;
		List<URL> testClassUrls = new ArrayList<>();
		String testClassPackagePath ="file:" + System.getProperty("user.dir") + File.separator + "target" + File.separator + "test-classes" + File.separator;
		// Add test packages to item list
		Collections.addAll(items, pack.split("\\s*,\\s*"));

		// Add URL for each test package
		for (int i = 0; i < items.size(); i++) 
		{
			testClassUrl = new URL(testClassPackagePath + items.get(i).replaceAll("\\.", "/"));
			testClassUrls.add(testClassUrl);
		}

		// Find test class by annotation: org.testng.annotations.Test.class
		Reflections reflections = new Reflections(new ConfigurationBuilder().setUrls(testClassUrls).setScanners(new MethodAnnotationsScanner()));
		Set<Method> resources = reflections.getMethodsAnnotatedWith(org.testng.annotations.Test.class);

		Map<String, List<Method>> methods = createTestsMap(resources);

		ExecutorService executor = Executors.newCachedThreadPool();
		List<FutureTask<Boolean>> list = new ArrayList<>();

		String[] browsers = RunTimeContext.getInstance().getProperty("BROWSER_TYPE").split(",");
		for(String browser : browsers)
		{
			XmlSuite suite = constructXmlSuite(browser, pack, test, methods);
			String suiteFile = writeTestNGFile(suite, "testsuite"  + "-" + browser);

			FutureTask<Boolean> futureTask = new FutureTask<>(new TestExecutorService(suiteFile));
			list.add(futureTask);

			executor.submit(futureTask);
		}

		// Wait for the test completion
		while(true)
		{
			boolean isDone = true;
			for(FutureTask<Boolean> futureTask : list)
			{
				isDone = isDone && futureTask.isDone();
			}

			if(isDone)
			{
				// Shutdown executor service
				executor.shutdown();
				break;
			}
			else
			{
				TimeUnit.SECONDS.sleep(1);
			}
		}

		boolean	hasFailure = false;
		//  Get the result
		for(FutureTask<Boolean> result : list)
		{
			hasFailure = hasFailure || result.get();
		}



		Figlet.print("Test Completed");

		return hasFailure;
	}

	public XmlSuite constructXmlSuite(String browser, String pack, List<String> tests, Map<String, List<Method>> methods) 
	{
		ArrayList<String> listeners = new ArrayList<>();
		ArrayList<String> groupsInclude = new ArrayList<>();
		ArrayList<String> groupsExclude = new ArrayList<>();

		// Add groups
		include(groupsInclude, "INCLUDE_GROUPS");
		include(groupsExclude, "EXCLUDE_GROUPS");

		// Initialize XML Suite
		XmlSuite suite = new XmlSuite();
		suite.setName("Test Suite");
		suite.setPreserveOrder(true);

		/*
		 *  Set parallel mode to METHODS level
		 *  Each method will be taken care of by 1 thread
		 */
		suite.setThreadCount(Integer.parseInt(context.getProperty("THREAD_COUNT")));
		suite.setDataProviderThreadCount(Integer.parseInt(context.getProperty("DATAPROVIDER_THREAD_COUNT")));
		suite.setParallel(ParallelMode.METHODS);
		suite.setVerbose(2);

		// Add listeners
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

	public void writeXmlClass(List<String> testcases, Map<String, List<Method>> methods, List<XmlClass> xmlClasses) 
	{
		for (String className : methods.keySet()) 
		{
			if (className.contains("Test")) 
			{
				if (testcases.size() == 0) 
				{
					xmlClasses.add(createClass(className, methods.get(className)));
				} 
				else 
				{
					for (String s : testcases) 
					{
						for (int j = 0; j < items.size(); j++)
						{
							String testName = items.get(j).concat("." + s).toString();
							if (testName.equals(className)) 
							{
								xmlClasses.add(createClass(className, methods.get(className)));
							}
						}
					}
				}

			}
		}
	}

	private String writeTestNGFile(XmlSuite suite, String fileName)
	{
		// Print out Suite XML
		System.out.println(suite.toXml());
		String suiteXML = System.getProperty("user.dir") + "/target/" + fileName + ".xml";

		try 
		{
			FileWriter writer = new FileWriter(new File(suiteXML));
			writer.write(suite.toXml());
			writer.flush();
			writer.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}

		return suiteXML;
	}

	public void include(ArrayList<String> groupsInclude, String include) 
	{
		if (context.getProperty(include) != null) 
		{
			Collections.addAll(groupsInclude, context.getProperty(include).split("\\s*,\\s*"));
		} 
		else if (System.getenv(include) != null) 
		{
			Collections.addAll(groupsInclude, System.getenv(include).split("\\s*,\\s*"));
		}
	}

	private XmlClass createClass(String className, List<Method> methods)
	{
		XmlClass clazz = new XmlClass();
		clazz.setName(className);
		return clazz;
	}


	public Map<String, List<Method>> createTestsMap(Set<Method> methods) 
	{
		Map<String, List<Method>> testsMap = new HashMap<>();
		methods.stream().forEach(method -> 
		{
			// Get method list from specific test class
			List<Method> methodsList = testsMap.get(method.getDeclaringClass().getPackage().getName() + "." + method.getDeclaringClass().getSimpleName());

			// If the method list is empty, initialize it and add it to test class map
			if (methodsList == null)
			{
				methodsList = new ArrayList<>();
				testsMap.put(method.getDeclaringClass().getPackage().getName() + "." + method.getDeclaringClass().getSimpleName(), methodsList);
			}

			// Add method to list
			methodsList.add(method);
		});

		return testsMap;
	}

}

class TestExecutorService implements Callable<Boolean>
{
	private String suite;

	public TestExecutorService(String file) 
	{
		suite = file;
	}

	@Override
	public Boolean call() throws Exception
	{
		List<String> suiteFiles = new ArrayList<>();
		suiteFiles.add(suite);

		TestNG testNG = new TestNG();
		testNG.setTestSuites(suiteFiles);
		testNG.run();

		return testNG.hasFailure();
	}

}
