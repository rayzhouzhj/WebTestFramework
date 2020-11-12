package com.scmp.framework.report;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.testng.ITestResult;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.scmp.framework.utils.ScreenShotManager;

/**
 *
 */
public class TestLogManager {
    private ScreenShotManager screenShotManager;

    public TestLogManager() {
        screenShotManager = new ScreenShotManager();
    }


    public void endLog(ITestResult result, ThreadLocal<ExtentTest> test) throws IOException, InterruptedException {
        String className = result.getInstance().getClass().getSimpleName();
        if (result.isSuccess()) {
            test.get().log(Status.PASS, "Test Passed: " + result.getMethod().getMethodName());
        } else {
            if (result.getStatus() == ITestResult.FAILURE) {
                /*
                 * Failure Block
                 */
                handleTestFailure(result, className, test);
            }
        }

        /*
         * Skip block
         */
        if (result.getStatus() == ITestResult.SKIP) {
            test.get().log(Status.SKIP, "Test skipped");
        }

        ExtentManager.getExtent().flush();
    }

    public String getClassName(String s) {
        final String classNameCur = s.substring(1);
        final Package[] packages = Package.getPackages();
        String className = null;

        for (final Package p : packages) {
            final String pack = p.getName();
            final String tentative = pack + "." + classNameCur;
            try {
                Class.forName(tentative);
            } catch (final ClassNotFoundException e) {
                continue;
            }

            className = tentative;
            break;
        }

        return className;
    }

    private void handleTestFailure(ITestResult result, String className, ThreadLocal<ExtentTest> test) {
        if (result.getStatus() == ITestResult.FAILURE) {
            // Print exception stack trace if any
            Throwable throwable = result.getThrowable();
            if (throwable != null) {
                throwable.printStackTrace();
                test.get().log(Status.FAIL, "<pre>" + result.getThrowable().getMessage() + "</pre>");
            }

            try {
                String screenShotAbsolutePath = screenShotManager.captureScreenShot(
                        Status.FAIL,
                        result.getInstance().getClass().getSimpleName(),
                        result.getMethod().getMethodName());

                String screenShotRelativePath = getRelativePathToReport(screenShotAbsolutePath);

                test.get().addScreenCaptureFromPath(screenShotRelativePath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getRelativePathToReport(String file) {
        Path path = new File(file).toPath();
        Path targetPath = new File(System.getProperty("user.dir") + File.separator + "target").toPath();
        return targetPath.relativize(path).toString();
    }
}
