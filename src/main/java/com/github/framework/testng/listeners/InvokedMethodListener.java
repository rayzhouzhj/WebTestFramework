package com.github.framework.testng.listeners;

import java.io.IOException;
import java.lang.reflect.Method;

import com.github.framework.annotations.AcceptUntrustedCertificates;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.annotations.Test;

import com.github.framework.annotations.ChromeArguments;
import com.github.framework.annotations.Description;
import com.github.framework.annotations.HeadlessMode;
import com.github.framework.annotations.screens.Mobile;
import com.github.framework.manager.WebDriverManager;
import com.github.framework.report.ExtentManager;
import com.github.framework.report.ReportManager;

public final class InvokedMethodListener implements IInvokedMethodListener
{
	private WebDriverManager driverManager;

	public InvokedMethodListener() throws Exception 
	{
		try 
		{
			driverManager = new WebDriverManager();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	private void resetReporter(IInvokedMethod method, ITestResult testResult)
	{
		Method refMethod = method.getTestMethod().getConstructorOrMethod().getMethod();
		String className = refMethod.getDeclaringClass().getSimpleName();

		// Create test node for test class in test report
		try 
		{
			String testDescription = "";
			if (testResult.getTestClass().getClass().getAnnotation(Description.class) != null) 
			{
				testDescription = getClass().getAnnotation(Description.class).value();
			}

			// Create test node at test class level
			ReportManager.getInstance().createParentNodeExtent(className, testDescription);
			ReportManager.getInstance().setTestResult(testResult);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

	/**
	 * Before each method invocation
	 * Initialize Web Driver and Report Manager
	 */
	@Override
	public void beforeInvocation(IInvokedMethod method, ITestResult testResult) 
	{
		Method refMethod = method.getTestMethod().getConstructorOrMethod().getMethod();
		String methodName = refMethod.getName();

		// Skip beforeInvocation if current method is not with Annotation Test
		if(refMethod.getAnnotation(Test.class) == null)
		{
			return;
		}
		
		System.out.println("[INFO] Start running test [" + methodName + "]");
		resetReporter(method, testResult);
		setupDriverForTest(method, refMethod);
		
	}

	private void setupDriverForTest(IInvokedMethod method, Method refMethod) 
	{
		String browserType = method.getTestMethod().getXmlTest().getParameter("browser");
		DesiredCapabilities browser = null;

		switch (browserType) 
		{
		case "chrome": {

			browser = DesiredCapabilities.chrome();
			ChromeOptions options = new ChromeOptions();
			
			// Get Chrome options/arguments
			ChromeArguments chromeArguments = refMethod.getAnnotation(ChromeArguments.class);
			if(chromeArguments != null && chromeArguments.options().length > 0) {
				options.addArguments(chromeArguments.options());
			}
			
			HeadlessMode headlessMode = refMethod.getAnnotation(HeadlessMode.class);
			// headless mode
			options.setHeadless(headlessMode == null? false : true);

			AcceptUntrustedCertificates acceptUntrustedCertificates = refMethod.getAnnotation(AcceptUntrustedCertificates.class);
			// Accept untrusted certificates
			options.setAcceptInsecureCerts(acceptUntrustedCertificates == null? false : true);

			browser.merge(options);
			
			break;
		}
		case "firefox": {

			browser = DesiredCapabilities.firefox();
			FirefoxOptions options = new FirefoxOptions();
			HeadlessMode headlessMode = refMethod.getAnnotation(HeadlessMode.class);
			// headless mode
			options.setHeadless(headlessMode == null? false : true);

			AcceptUntrustedCertificates acceptUntrustedCertificates = refMethod.getAnnotation(AcceptUntrustedCertificates.class);
			// Accept untrusted certificates
			options.setAcceptInsecureCerts(acceptUntrustedCertificates == null? false : true);

			browser.merge(options);
			
			break;
		}
		default:
			break;
		}

		// Check the mobile screen size preference
		Mobile mobileAnnotationData = refMethod.getAnnotation(Mobile.class);
		Dimension mobileDimension = null;
		if(mobileAnnotationData != null)
		{
			mobileDimension = new Dimension(mobileAnnotationData.width(), mobileAnnotationData.height());
		}
		driverManager.startDriverInstance(browser, mobileDimension);
		
		try 
		{
			// Update Author and set category
			ReportManager.getInstance().setAuthorName(method);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

	/**
	 * After each method invocation
	 * Update test result to report manager and stop Web Driver
	 */
	@Override
	public void afterInvocation(IInvokedMethod method, ITestResult testResult) 
	{
		Method refMethod = method.getTestMethod().getConstructorOrMethod().getMethod();
		String methodName = refMethod.getName();

		System.out.println("[INFO] Completed running test [" + methodName + "]");

		// Skip afterInvocation if current method is not with Annotation Test
		if(refMethod.getAnnotation(Test.class) == null)
		{
			return;
		}

		try 
		{
			if (testResult.getStatus() == ITestResult.SUCCESS || testResult.getStatus() == ITestResult.FAILURE) 
			{
				ReportManager.getInstance().endLogTestResults(testResult);
				ExtentManager.getExtent().flush();
			}
			else if (testResult.getStatus() == ITestResult.SKIP) 
			{
				ExtentManager.getExtent().flush();

				// Remove previous log data for retry test
				ReportManager.getInstance().removeTest();
			}

			driverManager.stopWebDriver();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}

	}
}
