package com.github.framework.report;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import org.testng.IInvokedMethod;
import org.testng.ITestResult;
import org.testng.annotations.Test;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.github.framework.annotation.values.Author;
import com.github.framework.utils.MethodDescription;
import com.github.framework.utils.ScreenShotManager;

/**
 * ReportManager - Handles all Reporting activities e.g communication with ExtentManager, etc
 */
public class ReportManager 
{
	private TestLogger testLogger;
	private static ReportManager manager = new ReportManager();
	public ThreadLocal<ExtentTest> parentTestClass = new ThreadLocal<>();
	public ThreadLocal<ExtentTest> currentTestMethod = new ThreadLocal<>();
	public ThreadLocal<ITestResult> testResult = new ThreadLocal<>();
	private ScreenShotManager screenshotManager = new ScreenShotManager();

	private ConcurrentHashMap<String, Boolean> retryMap = new ConcurrentHashMap<>();

	public static ReportManager getInstance()
	{
		return manager;
	}

	private ReportManager() 
	{
		testLogger = new TestLogger();
	}

	public void removeTest()
	{
		ExtentTestManager.removeTest(currentTestMethod.get());

	}
	
	public boolean isRetryMethod(String methodName, String className)
	{
		String key = className + ":" + methodName + Thread.currentThread().getId();
		if(!this.retryMap.containsKey(key))
		{
			this.retryMap.put(key, false);
		}
		
		return this.retryMap.get(key);
	}

	public void setMethodRetryStatus(String methodName, String className, boolean status)
	{
		String key = className + ":" + methodName + Thread.currentThread().getId();
		this.retryMap.put(key, status);
	}

	public void endLogTestResults(ITestResult result) throws IOException, InterruptedException 
	{
		testLogger.endLog(result, currentTestMethod);
	}

	public void setTestResult(ITestResult testResult)
	{
		this.testResult.set(testResult);
	}
	
	public ExtentTest createParentNodeExtent(String className, String classDescription) throws Exception 
	{
		ExtentTest parent = ExtentTestManager.createTest(className, classDescription);
		parentTestClass.set(parent);

		return parent;
	}

	public void setAuthorName(IInvokedMethod invokedMethod) throws Exception 
	{
		String authorName;
		String dataProvider = null;
		ArrayList<String> listeners = new ArrayList<>();
		Method method = invokedMethod.getTestMethod().getConstructorOrMethod().getMethod();
		String description = method.getAnnotation(Test.class).description();
		Object dataParameter = invokedMethod.getTestResult().getParameters();

		if (((Object[]) dataParameter).length > 0)
		{
			dataProvider = (String) ((Object[]) dataParameter)[0];
		}


		MethodDescription methodDescription = new MethodDescription(invokedMethod, description);
		boolean authorNamePresent = methodDescription.isAuthorNamePresent();
		String descriptionMethodName = methodDescription.getDescriptionMethodName();
		String category = "";

		String testName = dataProvider == null ? descriptionMethodName : descriptionMethodName + "[" + dataProvider + "]";
		if (authorNamePresent)
		{
			authorName = method.getAnnotation(Author.class).name();
			Collections.addAll(listeners, authorName.split("\\s*,\\s*"));
			ExtentTest child = parentTestClass.get().createNode(testName, category).assignAuthor(String.valueOf(listeners));
			child.assignCategory(category);
			currentTestMethod.set(child);
		} 
		else 
		{
			ExtentTest child = parentTestClass.get().createNode(testName, category);
			child.assignCategory(category);
			currentTestMethod.set(child);
		}
	}

	public void logInfo(String message)
	{
		this.currentTestMethod.get().log(Status.INFO, message);
	}

	public void logPass(String message)
	{
		this.currentTestMethod.get().log(Status.PASS, message);
	}

	public void logFail(String message)
	{
		String[] classAndMethod = getTestClassNameAndMethodName().split(",");
		try 
		{
			String screenShot = screenshotManager.captureScreenShot(1, classAndMethod[0], classAndMethod[1]);
			this.currentTestMethod.get().log(Status.FAIL, message);
			this.currentTestMethod.get().addScreenCaptureFromPath(screenShot);
			this.testResult.get().setStatus(ITestResult.FAILURE);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private String getTestClassNameAndMethodName()
	{
		String classAndMethod = "";

		Exception ex = new Exception();
		StackTraceElement[] stacks = ex.getStackTrace();
		for(StackTraceElement e : stacks)
		{
			classAndMethod = e.getClassName() + "," + e.getMethodName();

			if(e.getClassName().startsWith("com.scmp.test") && e.getMethodName().startsWith("test"))
			{
				classAndMethod = e.getClassName().substring(e.getClassName().lastIndexOf(".") + 1) + "," + e.getMethodName();

				break;
			}
		}

		return classAndMethod;
	}
}
