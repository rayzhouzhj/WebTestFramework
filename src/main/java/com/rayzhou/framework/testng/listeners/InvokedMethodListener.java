package com.rayzhou.framework.testng.listeners;

import java.lang.reflect.Method;

import com.rayzhou.framework.annotations.*;
import com.rayzhou.framework.annotations.screens.DeviceName;
import com.rayzhou.framework.context.RunTimeContext;
import com.rayzhou.framework.testng.model.TestInfo;
import com.rayzhou.framework.annotations.screens.Device;
import com.rayzhou.framework.manager.WebDriverManager;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.testng.*;
import org.testng.annotations.Test;

import com.rayzhou.framework.report.ExtentManager;
import com.rayzhou.framework.report.ReportManager;


public final class InvokedMethodListener implements IInvokedMethodListener {
    private WebDriverManager driverManager;

    public InvokedMethodListener() {
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
        try {
            String browserType = setupDriverForTest(testInfo, testResult);
            // Update Author and set categories
            ReportManager.getInstance().setTestInfo(testInfo.getInvokedMethod());
            ReportManager.getInstance().addTag(browserType.toUpperCase());
            ReportManager.getInstance().setSetupStatus(true);
        } catch (Exception ex) {
            ex.printStackTrace();
            ReportManager.getInstance().setSetupStatus(false);
            Assert.fail("Fails to setup test driver.");
        }
    }

    private String setupDriverForTest(TestInfo testInfo, ITestResult testResult) throws Exception {

        // Get browser type from retry method
        String retryBrowserType = "";
        IRetryAnalyzer analyzer = testResult.getMethod().getRetryAnalyzer();
        if (analyzer instanceof RetryAnalyzer) {
            retryBrowserType = ((RetryAnalyzer) analyzer).getRetryMethod(testResult).getBrowserType();
        }

        String browserType = testInfo.getInvokedMethod().getTestMethod().getXmlTest().getParameter("browser");

        // Override browser type
        FirefoxOnly firefoxOnly = testInfo.getDeclaredMethod().getAnnotation(FirefoxOnly.class);
        ChromeOnly chromeOnly = testInfo.getDeclaredMethod().getAnnotation(ChromeOnly.class);
        if (!retryBrowserType.isEmpty()) {
            browserType = retryBrowserType;
        } else if (firefoxOnly != null) {
            browserType = "firefox";
        } else if (chromeOnly != null) {
            browserType = "chrome";
        } else if ("random".equalsIgnoreCase(browserType)) {
            browserType = Math.round(Math.random()) == 1 ? "chrome" : "firefox";
        }

        // Update browser type to retry method
        ((RetryAnalyzer) analyzer).getRetryMethod(testResult).setBrowserType(browserType);

        // Reset report data
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
        Device deviceAnnotationData = testInfo.getDeclaredMethod().getAnnotation(Device.class);
        Dimension deviceDimension;
        if (deviceAnnotationData != null) {
            int width = deviceAnnotationData.device() == DeviceName.OtherDevice ? deviceAnnotationData.width() : deviceAnnotationData.device().width;
            int height = deviceAnnotationData.device() == DeviceName.OtherDevice ? deviceAnnotationData.height() : deviceAnnotationData.device().height;
            deviceDimension = new Dimension(width, height);
        } else {
            // If device dimension is not specified, use desktop by default
            deviceDimension = new Dimension(DeviceName.DeskTopHD.width, DeviceName.DeskTopHD.height);
        }

        try {
            // Setup web driver
            driverManager.startDriverInstance(browserOptions, deviceDimension);
        } catch (Exception ex1) {
            if(!RunTimeContext.getInstance().isDebugMode()) {
                try {
                    driverManager.stopWebDriver();
                    // Wait 30 seconds and retry driver setup
                    Thread.sleep(30000);
                    // Setup web driver
                    driverManager.startDriverInstance(browserOptions, deviceDimension);
                } catch (Exception ex2) {
                    throw ex2;
                }
            } else {
                throw ex1;
            }
        }

        return browserType;
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

        // If fails to setup test
        if (!ReportManager.getInstance().getSetupStatus()) {
            driverManager.stopWebDriver();

            return;
        }

        try {
            if (testResult.getStatus() == ITestResult.SUCCESS || testResult.getStatus() == ITestResult.FAILURE) {

                IRetryAnalyzer analyzer = testResult.getMethod().getRetryAnalyzer();
                if (analyzer instanceof RetryAnalyzer) {
                    if (((RetryAnalyzer) analyzer).isRetriedMethod(testResult) ||
                            testResult.getStatus() == ITestResult.FAILURE) {
                        ReportManager.getInstance().addTag("RETRIED");
                    }
                }

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
