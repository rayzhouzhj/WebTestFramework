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
import com.github.framework.annotations.Author;
import com.github.framework.utils.ScreenShotManager;

/**
 * ReportManager - Handles all Reporting activities e.g communication with ExtentManager, etc
 */
public class ReportManager {
    private TestLogManager testLogger;
    private static ReportManager manager = new ReportManager();
    public ThreadLocal<ExtentTest> ParentTestClass = new ThreadLocal<>();
    public ThreadLocal<ExtentTest> CurrentTestMethod = new ThreadLocal<>();
    public ThreadLocal<ITestResult> TestResult = new ThreadLocal<>();
    public ScreenShotManager ScreenshotManager = new ScreenShotManager();

    private ConcurrentHashMap<String, Boolean> retryMap = new ConcurrentHashMap<>();

    public static ReportManager getInstance() {
        return manager;
    }

    private ReportManager() {
        testLogger = new TestLogManager();
    }

    public void removeTest() {
        ExtentTestManager.removeTest(CurrentTestMethod.get());

    }

    public boolean isRetryMethod(String methodName, String className) {
        String key = className + ":" + methodName + Thread.currentThread().getId();
        if (!this.retryMap.containsKey(key)) {
            this.retryMap.put(key, false);
        }

        return this.retryMap.get(key);
    }

    public void setMethodRetryStatus(String methodName, String className, boolean status) {
        String key = className + ":" + methodName + Thread.currentThread().getId();
        this.retryMap.put(key, status);
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

    public ExtentTest setupReportForTestSet(String className, String classDescription) throws Exception {
        ExtentTest parent = ExtentTestManager.createTest(className, classDescription);
        ParentTestClass.set(parent);

        return parent;
    }

    public void setAuthorName(IInvokedMethod invokedMethod) throws Exception {
        String authorName;
        String dataProvider = null;
        ArrayList<String> listeners = new ArrayList<>();
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
        if (authorNamePresent) {
            authorName = method.getAnnotation(Author.class).name();
            Collections.addAll(listeners, authorName.split("\\s*,\\s*"));
            ExtentTest child = ParentTestClass.get().createNode(testName, category).assignAuthor(String.valueOf(listeners));
            child.assignCategory(category);
            CurrentTestMethod.set(child);
        } else {
            ExtentTest child = ParentTestClass.get().createNode(testName, category);
            child.assignCategory(category);
            CurrentTestMethod.set(child);
        }
    }

    public void logInfo(String message) {
        this.CurrentTestMethod.get().log(Status.INFO, message);
    }

    public void logPass(String message) {
        this.CurrentTestMethod.get().log(Status.PASS, message);
    }

    public void logFail(String message) {
        String[] classAndMethod = getTestClassNameAndMethodName().split(",");
        try {
            String screenShot = ScreenshotManager.captureScreenShot(Status.FAIL, classAndMethod[0], classAndMethod[1]);
            this.CurrentTestMethod.get().log(Status.FAIL, message);
            this.CurrentTestMethod.get().addScreenCaptureFromPath(screenShot);
            this.TestResult.get().setStatus(ITestResult.FAILURE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void logInfoWithScreenShot(String message) {
        try {
            String[] classAndMethod = getTestClassNameAndMethodName().split(",");
            String screenShot = ScreenshotManager.captureScreenShot(Status.INFO, classAndMethod[0], classAndMethod[1]);
            this.CurrentTestMethod.get().log(Status.INFO, message);
            this.CurrentTestMethod.get().addScreenCaptureFromPath(screenShot);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
