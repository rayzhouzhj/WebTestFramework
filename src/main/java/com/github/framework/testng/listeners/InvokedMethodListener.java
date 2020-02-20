package com.github.framework.testng.listeners;

import java.lang.reflect.Method;

import com.github.framework.annotations.*;
import com.github.framework.annotations.screens.DeviceName;
import com.github.framework.testng.model.TestInfo;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.annotations.Test;

import com.github.framework.annotations.screens.Mobile;
import com.github.framework.manager.WebDriverManager;
import com.github.framework.report.ExtentManager;
import com.github.framework.report.ReportManager;


public final class InvokedMethodListener implements IInvokedMethodListener {
    private WebDriverManager driverManager;

    public InvokedMethodListener() throws Exception {
        driverManager = new WebDriverManager();
    }

    private void resetReporter(IInvokedMethod method, ITestResult testResult) {
        Method refMethod = method.getTestMethod().getConstructorOrMethod().getMethod();
        String className = refMethod.getDeclaringClass().getSimpleName();

        // Create test node for test class in test report
        try {
            String testDescription = "";
            if (testResult.getTestClass().getClass().getAnnotation(TestDescription.class) != null) {
                testDescription = getClass().getAnnotation(TestDescription.class).value();
            }

            // Create test node at test class level
            ReportManager.getInstance().setupReportForTestSet(className, testDescription);
            ReportManager.getInstance().setTestResult(testResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Before each method invocation
     * Initialize Web Driver and Report Manager
     */
    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {

        TestInfo testInfo = new TestInfo(method);

        // Skip beforeInvocation if current method is not with Annotation Test
        if (!testInfo.isTestMethod()) {
            return;
        }

        System.out.println("[INFO] Start running test [" + testInfo.getMethodName() + "]");
        setupDriverForTest(testInfo, testResult);

    }

    private void setupDriverForTest(TestInfo testInfo, ITestResult testResult) {
        String browserType = testInfo.getInvokedMethod().getTestMethod().getXmlTest().getParameter("browser");

        resetReporter(testInfo.getInvokedMethod(), testResult);

        MutableCapabilities browserOptions;
        switch (browserType) {
            case "chrome": {
                ChromeOptions options = new ChromeOptions();

                // Get Chrome options/arguments
                ChromeArguments chromeArguments = testInfo.getDeclaredMethod().getAnnotation(ChromeArguments.class);
                if (chromeArguments != null && chromeArguments.options().length > 0) {
                    options.addArguments(chromeArguments.options());
                }

                // private mode
                IncognitoPrivateMode privateMode = testInfo.getDeclaredMethod().getAnnotation(IncognitoPrivateMode.class);
                if (privateMode != null) {
                    options.addArguments("--incognito");
                }

                // headless mode
                HeadlessMode headlessMode = testInfo.getDeclaredMethod().getAnnotation(HeadlessMode.class);
                options.setHeadless(headlessMode != null);

                // Accept untrusted certificates
                AcceptUntrustedCertificates acceptUntrustedCertificates = testInfo.getDeclaredMethod().getAnnotation(AcceptUntrustedCertificates.class);
                options.setAcceptInsecureCerts(acceptUntrustedCertificates != null);

                browserOptions = options;

                break;
            }
            case "firefox": {
                FirefoxOptions options = new FirefoxOptions();
                // Get Firefox options/arguments
                FirefoxArguments firefoxArguments = testInfo.getDeclaredMethod().getAnnotation(FirefoxArguments.class);
                if (firefoxArguments != null && firefoxArguments.options().length > 0) {
                    options.addArguments(firefoxArguments.options());
                }

                // private mode
                IncognitoPrivateMode privateMode = testInfo.getDeclaredMethod().getAnnotation(IncognitoPrivateMode.class);
                if (privateMode != null) {
                    options.addArguments("-private");
                }

                // headless mode
                HeadlessMode headlessMode = testInfo.getDeclaredMethod().getAnnotation(HeadlessMode.class);
                options.setHeadless(headlessMode != null);

                // Accept untrusted certificates
                AcceptUntrustedCertificates acceptUntrustedCertificates = testInfo.getDeclaredMethod().getAnnotation(AcceptUntrustedCertificates.class);
                options.setAcceptInsecureCerts(acceptUntrustedCertificates != null);

                browserOptions = options;

                break;
            }
            default:
                throw new RuntimeException("Unsupported browser: " + browserType);
        }

        // Check the mobile screen size preference
        Mobile mobileAnnotationData = testInfo.getDeclaredMethod().getAnnotation(Mobile.class);
        Dimension deviceDimension;
        if (mobileAnnotationData != null) {
            int width = mobileAnnotationData.device() == DeviceName.OtherDevice ? mobileAnnotationData.width() : mobileAnnotationData.device().width;
            int height = mobileAnnotationData.device() == DeviceName.OtherDevice ? mobileAnnotationData.height() : mobileAnnotationData.device().height;
            deviceDimension = new Dimension(width, height);
        } else {
            // If device dimension is not specified, use desktop by default
            deviceDimension = new Dimension(DeviceName.DeskTopHD.width, DeviceName.DeskTopHD.height);
        }

        driverManager.startDriverInstance(browserOptions, deviceDimension);

        try {
            // Update Author and set category
            ReportManager.getInstance().setAuthorName(testInfo.getInvokedMethod());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * After each method invocation
     * Update test result to report manager and stop Web Driver
     */
    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        Method refMethod = method.getTestMethod().getConstructorOrMethod().getMethod();
        String methodName = refMethod.getName();

        System.out.println("[INFO] Completed running test [" + methodName + "]");

        // Skip afterInvocation if current method is not with Annotation Test
        if (refMethod.getAnnotation(Test.class) == null) {
            return;
        }

        try {
            if (testResult.getStatus() == ITestResult.SUCCESS || testResult.getStatus() == ITestResult.FAILURE) {
                ReportManager.getInstance().endLogTestResults(testResult);
                ExtentManager.getExtent().flush();
            } else if (testResult.getStatus() == ITestResult.SKIP) {
                ExtentManager.getExtent().flush();

                // Remove previous log data for retry test
                ReportManager.getInstance().removeTest();
            }

            driverManager.stopWebDriver();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
