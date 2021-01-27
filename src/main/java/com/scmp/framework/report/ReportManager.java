package com.scmp.framework.report;

import java.io.File;
import java.nio.file.Path;

import com.scmp.framework.context.RunTimeContext;
import com.scmp.framework.testng.listeners.RetryAnalyzer;
import com.scmp.framework.testng.model.TestInfo;
import com.scmp.framework.testrail.TestRailStatus;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.scmp.framework.utils.ScreenShotManager;

import static com.scmp.framework.utils.Constants.TARGET_PATH;
import static com.scmp.framework.utils.Constants.TEST_INFO_OBJECT;

/**
 * ReportManager - Handles all Reporting activities e.g communication with ExtentManager, etc
 */
public class ReportManager {
    private static ReportManager manager = new ReportManager();
    private ThreadLocal<ExtentTest> parentTestClass = new ThreadLocal<>();
    private ThreadLocal<ExtentTest> currentTestMethod = new ThreadLocal<>();
    private ThreadLocal<ITestResult> testResult = new ThreadLocal<>();
    private ThreadLocal<Boolean> setupStatus = new ThreadLocal<>();
    private ScreenShotManager screenshotManager = new ScreenShotManager();

    public static ReportManager getInstance() {
        return manager;
    }

    private ReportManager() {}

    public void removeTest() {
        ExtentTestManager.removeTest(currentTestMethod.get());
    }

    public void skipTest() {
        currentTestMethod.get().getModel().setStatus(Status.SKIP);
    }

    /**
     * Log details for failure case
     * 1. Print stack trace
     * 2. Capture screenshot
     * @param result
     * @param testInfo
     */
    private void handleTestFailure(ITestResult result, TestInfo testInfo) {
        if (result.getStatus() == ITestResult.FAILURE) {
            // Print exception stack trace if any
            Throwable throwable = result.getThrowable();
            if (throwable != null) {
                throwable.printStackTrace();
                currentTestMethod.get().log(Status.FAIL, "<pre>" + result.getThrowable().getMessage() + "</pre>");
            }

            try {
                String screenShotAbsolutePath =
                        screenshotManager.captureScreenShot(
                                Status.FAIL,
                                result.getInstance().getClass().getSimpleName(),
                                result.getMethod().getMethodName());

                String screenShotRelativePath = getRelativePathToReport(screenShotAbsolutePath);

                currentTestMethod.get().addScreenCaptureFromPath(screenShotRelativePath);

                testInfo
                        .getTestRailDataHandler()
                        .addStepResult(
                                TestRailStatus.Failed, result.getThrowable().getMessage(), screenShotRelativePath);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void endLogTestResults(ITestResult result) {
        TestInfo testInfo =
                (TestInfo) RunTimeContext.getInstance().getTestLevelVariables(TEST_INFO_OBJECT);

        if (result.isSuccess()) {
            String message = "Test Passed: " + result.getMethod().getMethodName();
            currentTestMethod.get().log(Status.PASS, message);
            testInfo.getTestRailDataHandler().addStepResult(TestRailStatus.Passed, message, null);

        } else {
            if (result.getStatus() == ITestResult.FAILURE) {
                /*
                 * Failure Block
                 */
                handleTestFailure(result, testInfo);
            }
        }

        if (result.getStatus() == ITestResult.SKIP) {
            currentTestMethod.get().log(Status.SKIP, "Test skipped");
        }

        ExtentManager.getExtent().flush();


        // Handling for Retry
        IRetryAnalyzer analyzer = result.getMethod().getRetryAnalyzer();
        if (analyzer instanceof RetryAnalyzer) {
            if (((RetryAnalyzer) analyzer).isRetriedMethod(result) ||
                    result.getStatus() == ITestResult.FAILURE) {
                this.addTag("RETRIED");
            }

            if (RunTimeContext.getInstance().removeFailedTestB4Retry()
                    && result.getStatus() == ITestResult.FAILURE
                    && ((RetryAnalyzer) analyzer).isRetriedRequired(result)) {
                this.removeTest();
            }
        }

        if (result.getStatus() == ITestResult.SKIP) {
            this.skipTest();
        }

        ExtentManager.flush();
    }

    public void setTestResult(ITestResult testResult) {
        this.testResult.set(testResult);
    }

    public void setSetupStatus(boolean status) {
        this.setupStatus.set(status);
    }

    public boolean getSetupStatus() {
        return this.setupStatus.get() == null? false : this.setupStatus.get();
    }

    public synchronized ExtentTest setupReportForTestSet(TestInfo testInfo) {
        ExtentTest parent = ExtentTestManager.createTest(testInfo.getClassName(), testInfo.getClassDescription());
        if(testInfo.getClassGroups() != null) {
            parent.assignCategory(testInfo.getClassGroups());
        }

        parentTestClass.set(parent);

        return parent;
    }

    public synchronized void setTestInfo(TestInfo testInfo) {

        ExtentTest child = parentTestClass.get().createNode(testInfo.getTestName(), testInfo.getTestMethodDescription());
        currentTestMethod.set(child);

        // Update authors
        if (testInfo.getAuthorNames() != null) {
            currentTestMethod.get().assignAuthor(testInfo.getAuthorNames());
        }

        // Update groups to category
        currentTestMethod.get().assignCategory(testInfo.getTestGroups());
    }

    public void addTag(String tag){
        this.currentTestMethod.get().assignCategory(tag);
    }

    public String getImagePath(String imageName) {
        String[] classAndMethod = getTestClassNameAndMethodName().split(",");
        try {
            return screenshotManager.getScreenshotPath(classAndMethod[0], classAndMethod[1], imageName);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void logInfo(String message) {
        this.currentTestMethod.get().log(Status.INFO, message);
    }

    public String logScreenshot() {
        return this.logScreenshot(Status.INFO);
    }

    public String logScreenshot(Status status) {
        try {
            String[] classAndMethod = getTestClassNameAndMethodName().split(",");
            String screenShotAbsolutePath = screenshotManager.captureScreenShot(Status.INFO, classAndMethod[0], classAndMethod[1]);
            String screenShotRelativePath = getRelativePathToReport(screenShotAbsolutePath);
            this.currentTestMethod.get().log(status,
                    "<img data-featherlight=" + screenShotRelativePath + " width=\"10%\" src=" + screenShotRelativePath + " data-src=" + screenShotRelativePath + ">");

            return screenShotAbsolutePath;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    public void logInfoWithScreenshot(String message) {
        this.currentTestMethod.get().log(Status.INFO, message);
        this.logScreenshot();
    }

    public void logPass(String message) {
        this.currentTestMethod.get().log(Status.PASS, message);
    }

    public void logPassWithScreenshot(String message) {
        this.currentTestMethod.get().log(Status.PASS, message);
        this.logScreenshot(Status.PASS);
    }

    public void logFail(String message) {
        this.currentTestMethod.get().log(Status.FAIL, message);
        this.logScreenshot(Status.FAIL);
        this.testResult.get().setStatus(ITestResult.FAILURE);
    }

    public void logFailWithoutScreenshot(String message) {
        this.currentTestMethod.get().log(Status.FAIL, message);
        this.testResult.get().setStatus(ITestResult.FAILURE);
    }

    public void logFailWithImage(String message, String imagePath) {
        imagePath = getRelativePathToReport(imagePath);
        try {
            this.currentTestMethod.get().log(Status.FAIL, message);
            this.currentTestMethod.get().log(Status.FAIL,
                    "<img data-featherlight=" + imagePath + " width=\"10%\" src=" + imagePath + " data-src=" + imagePath + ">");
            this.testResult.get().setStatus(ITestResult.FAILURE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String captureScreenShot() {
        try {
            String[] classAndMethod = getTestClassNameAndMethodName().split(",");
            return screenshotManager.captureScreenShot(Status.INFO, classAndMethod[0], classAndMethod[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void attachImage(String image) {
        try {
            this.currentTestMethod.get().addScreenCaptureFromPath(image);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getRelativePathToReport(String file) {
        Path path = new File(file).toPath();
        Path targetPath = new File(TARGET_PATH).toPath();
        return targetPath.relativize(path).toString();
    }

    private String getTestClassNameAndMethodName() {
        String classAndMethod = "";

        Exception ex = new Exception();
        StackTraceElement[] stacks = ex.getStackTrace();
        for (StackTraceElement e : stacks) {
            classAndMethod = e.getClassName() + "," + e.getMethodName();

            if (e.getMethodName().startsWith("test")) {
                classAndMethod = e.getClassName().substring(e.getClassName().lastIndexOf(".") + 1) + "," + e.getMethodName();

                break;
            }
        }

        return classAndMethod;
    }
}
