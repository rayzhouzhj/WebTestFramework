package com.scmp.framework.report;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import com.scmp.framework.context.RunTimeContext;
import com.scmp.framework.testng.listeners.RetryAnalyzer;
import com.scmp.framework.testng.model.TestInfo;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.scmp.framework.utils.ScreenShotManager;

/**
 * ReportManager - Handles all Reporting activities e.g communication with ExtentManager, etc
 */
public class ReportManager {
    private TestLogManager testLogger;
    private static ReportManager manager = new ReportManager();
    private ThreadLocal<ExtentTest> ParentTestClass = new ThreadLocal<>();
    private ThreadLocal<ExtentTest> CurrentTestMethod = new ThreadLocal<>();
    private ThreadLocal<ITestResult> TestResult = new ThreadLocal<>();
    private ThreadLocal<Boolean> SetupStatus = new ThreadLocal<>();
    private ScreenShotManager ScreenshotManager = new ScreenShotManager();

    public static ReportManager getInstance() {
        return manager;
    }

    private ReportManager() {
        testLogger = new TestLogManager();
    }

    public void removeTest() {
        ExtentTestManager.removeTest(CurrentTestMethod.get());
    }

    public void skipTest() {
        CurrentTestMethod.get().getModel().setStatus(Status.SKIP);
    }

    public void endLogTestResults(ITestResult result) throws IOException, InterruptedException {
        testLogger.endLog(result, CurrentTestMethod);

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
        this.TestResult.set(testResult);
    }

    public void setSetupStatus(boolean status) {
        this.SetupStatus.set(status);
    }

    public boolean getSetupStatus() {
        return this.SetupStatus.get() == null? false : this.SetupStatus.get();
    }

    public synchronized ExtentTest setupReportForTestSet(TestInfo testInfo) {
        ExtentTest parent = ExtentTestManager.createTest(testInfo.getClassName(), testInfo.getClassDescription());
        if(testInfo.getClassGroups() != null) {
            parent.assignCategory(testInfo.getClassGroups());
        }

        ParentTestClass.set(parent);

        return parent;
    }

    public synchronized void setTestInfo(TestInfo testInfo) {

        ExtentTest child = ParentTestClass.get().createNode(testInfo.getTestName(), testInfo.getTestMethodDescription());
        CurrentTestMethod.set(child);

        // Update authors
        if (testInfo.getAuthorNames() != null) {
            CurrentTestMethod.get().assignAuthor(testInfo.getAuthorNames());
        }

        // Update groups to category
        CurrentTestMethod.get().assignCategory(testInfo.getTestGroups());
    }

    public void addTag(String tag){
        this.CurrentTestMethod.get().assignCategory(tag);
    }

    public String getImagePath(String imageName) {
        String[] classAndMethod = getTestClassNameAndMethodName().split(",");
        try {
            return ScreenshotManager.getScreenshotPath(classAndMethod[0], classAndMethod[1], imageName);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void logInfo(String message) {
        this.CurrentTestMethod.get().log(Status.INFO, message);
    }

    public String logScreenshot() {
        return this.logScreenshot(Status.INFO);
    }

    public String logScreenshot(Status status) {
        try {
            String[] classAndMethod = getTestClassNameAndMethodName().split(",");
            String screenShotAbsolutePath = ScreenshotManager.captureScreenShot(Status.INFO, classAndMethod[0], classAndMethod[1]);
            String screenShotRelativePath = getRelativePathToReport(screenShotAbsolutePath);
            this.CurrentTestMethod.get().log(status,
                    "<img data-featherlight=" + screenShotRelativePath + " width=\"10%\" src=" + screenShotRelativePath + " data-src=" + screenShotRelativePath + ">");

            return screenShotAbsolutePath;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    public void logInfoWithScreenshot(String message) {
        this.CurrentTestMethod.get().log(Status.INFO, message);
        this.logScreenshot();
    }

    public void logPass(String message) {
        this.CurrentTestMethod.get().log(Status.PASS, message);
    }

    public void logPassWithScreenshot(String message) {
        this.CurrentTestMethod.get().log(Status.PASS, message);
        this.logScreenshot(Status.PASS);
    }

    public void logFail(String message) {
        this.CurrentTestMethod.get().log(Status.FAIL, message);
        this.logScreenshot(Status.FAIL);
        this.TestResult.get().setStatus(ITestResult.FAILURE);
    }

    public void logFailWithoutScreenshot(String message) {
        this.CurrentTestMethod.get().log(Status.FAIL, message);
        this.TestResult.get().setStatus(ITestResult.FAILURE);
    }

    public void logFailWithImage(String message, String imagePath) {
        imagePath = getRelativePathToReport(imagePath);
        try {
            this.CurrentTestMethod.get().log(Status.FAIL, message);
            this.CurrentTestMethod.get().log(Status.FAIL,
                    "<img data-featherlight=" + imagePath + " width=\"10%\" src=" + imagePath + " data-src=" + imagePath + ">");
            this.TestResult.get().setStatus(ITestResult.FAILURE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String captureScreenShot() {
        try {
            String[] classAndMethod = getTestClassNameAndMethodName().split(",");
            return ScreenshotManager.captureScreenShot(Status.INFO, classAndMethod[0], classAndMethod[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void attachImage(String image) {
        try {
            this.CurrentTestMethod.get().addScreenCaptureFromPath(image);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getRelativePathToReport(String file) {
        Path path = new File(file).toPath();
        Path targetPath = new File(System.getProperty("user.dir") + File.separator + "target").toPath();
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
