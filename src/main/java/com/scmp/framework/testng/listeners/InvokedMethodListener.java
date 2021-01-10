package com.scmp.framework.testng.listeners;

import java.lang.reflect.Method;

import com.scmp.framework.context.RunTimeContext;
import com.scmp.framework.testng.model.TestInfo;
import com.scmp.framework.manager.WebDriverManager;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.MutableCapabilities;
import org.testng.*;
import org.testng.annotations.Test;

import com.scmp.framework.report.ExtentManager;
import com.scmp.framework.report.ReportManager;


public final class InvokedMethodListener implements IInvokedMethodListener {
    private WebDriverManager driverManager;

    public InvokedMethodListener() {
        driverManager = new WebDriverManager();
    }

    /**
     * Setup reporter in report manager
     *
     * @param testInfo
     */
    private void setupReporterForTest(TestInfo testInfo) {
        try {
            // Create test node for test class in test report
            ReportManager.getInstance().setupReportForTestSet(testInfo);
            ReportManager.getInstance().setTestResult(testInfo.getTestResult());

            // Create test case in test report
            ReportManager.getInstance().setTestInfo(testInfo);
            ReportManager.getInstance().addTag(testInfo.getBrowserType().toString());
            ReportManager.getInstance().setSetupStatus(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Setup web driver for current test
     *
     * @param testInfo
     * @throws Exception
     */
    private void setupDriverForTest(TestInfo testInfo) throws Exception {

        MutableCapabilities browserOptions = testInfo.getBrowserOption();
        Dimension deviceDimension = testInfo.getDeviceDimension();

        try {
            // Setup web driver
            driverManager.startDriverInstance(browserOptions, deviceDimension);
        } catch (Exception ex1) {
            if (!RunTimeContext.getInstance().isLocalExecutionMode()) {
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
    }

    /**
     * Before each method invocation
     * Initialize Web Driver and Report Manager
     */
    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {

        TestInfo testInfo = new TestInfo(method, testResult);

        // Skip beforeInvocation if current method is not with Annotation Test
        if (!testInfo.isTestMethod()) {
            return;
        }

        System.out.println("[INFO] Start running test [" + testInfo.getMethodName() + "]");
        try {
            setupDriverForTest(testInfo);
            setupReporterForTest(testInfo);
        } catch (Exception ex) {
            ex.printStackTrace();
            ReportManager.getInstance().setSetupStatus(false);
            Assert.fail("Fails to setup test driver.");
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

        // If fails to setup test
        if (!ReportManager.getInstance().getSetupStatus()) {
            driverManager.stopWebDriver();

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
