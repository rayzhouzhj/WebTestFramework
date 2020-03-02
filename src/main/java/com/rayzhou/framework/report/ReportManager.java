package com.rayzhou.framework.report;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;

import com.rayzhou.framework.annotations.Author;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.testng.IInvokedMethod;
import org.testng.ITestResult;
import org.testng.annotations.Test;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.rayzhou.framework.utils.ScreenShotManager;

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

    public void endLogTestResults(ITestResult result) throws IOException, InterruptedException {
        testLogger.endLog(result, CurrentTestMethod);

        ExtentManager.flush();

        if (result.getStatus() == ITestResult.SKIP) {
            // Remove previous log data for retry test
            this.removeTest();
        }
    }

    public void setTestResult(ITestResult testResult) {
        this.TestResult.set(testResult);
    }

    public void setSetupStatus(boolean status) {
        this.SetupStatus.set(status);
    }

    public boolean getSetupStatus() {
        return this.SetupStatus.get();
    }

    public ExtentTest setupReportForTestSet(String className, String classDescription) {
        ExtentTest parent = ExtentTestManager.createTest(className, classDescription);
        ParentTestClass.set(parent);

        return parent;
    }

    public void setTestInfo(IInvokedMethod invokedMethod) {
        String authorName;
        String dataProvider = null;
        ArrayList<String> authors = new ArrayList<>();
        Method method = invokedMethod.getTestMethod().getConstructorOrMethod().getMethod();
        String description = method.getAnnotation(Test.class).description();
        Object dataParameter = invokedMethod.getTestResult().getParameters();

        if (((Object[]) dataParameter).length > 0) {
            dataProvider = (String) ((Object[]) dataParameter)[0];
        }

        ExtentTestDescription methodDescription = new ExtentTestDescription(invokedMethod, description);
        boolean authorNamePresent = methodDescription.isAuthorNamePresent();
        String descriptionMethodName = methodDescription.getDescriptionMethodName();
        String category = invokedMethod.getTestMethod().getXmlTest().getParameter("browser");

        String testName = dataProvider == null ? descriptionMethodName : descriptionMethodName + "[" + dataProvider + "]";
        ExtentTest child = ParentTestClass.get().createNode(testName, category);
        child.assignCategory(category);
        CurrentTestMethod.set(child);

        if (authorNamePresent) {
            authorName = method.getAnnotation(Author.class).name();
            Collections.addAll(authors, authorName.split("\\s*,\\s*"));
            CurrentTestMethod.get().assignAuthor(String.valueOf(authors));
        }

        // Update groups to category
        String[] groups = invokedMethod.getTestMethod().getGroups();
        CurrentTestMethod.get().assignCategory(groups);
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
        try {
            this.CurrentTestMethod.get().log(Status.INFO, message);
            this.logScreenshot();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        try {
            this.CurrentTestMethod.get().log(Status.FAIL, message);
            this.TestResult.get().setStatus(ITestResult.FAILURE);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
