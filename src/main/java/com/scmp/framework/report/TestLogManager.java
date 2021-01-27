package com.scmp.framework.report;

import java.io.File;
import java.nio.file.Path;

import com.scmp.framework.context.RunTimeContext;
import com.scmp.framework.testng.model.TestInfo;
import com.scmp.framework.testrail.TestRailStatus;
import org.testng.ITestResult;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.scmp.framework.utils.ScreenShotManager;

import static com.scmp.framework.utils.Constants.*;


public class TestLogManager {
  private ScreenShotManager screenShotManager;

  public TestLogManager() {
    screenShotManager = new ScreenShotManager();
  }

  public void endLog(ITestResult result, ThreadLocal<ExtentTest> test) {
    if (result.isSuccess()) {
      test.get().log(Status.PASS, "Test Passed: " + result.getMethod().getMethodName());
    } else {
      if (result.getStatus() == ITestResult.FAILURE) {
        /*
         * Failure Block
         */
        handleTestFailure(result, test);
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

  private void handleTestFailure(ITestResult result, ThreadLocal<ExtentTest> test) {
    if (result.getStatus() == ITestResult.FAILURE) {
      // Print exception stack trace if any
      Throwable throwable = result.getThrowable();
      if (throwable != null) {
        throwable.printStackTrace();
        test.get().log(Status.FAIL, "<pre>" + result.getThrowable().getMessage() + "</pre>");
      }

      try {
        String screenShotAbsolutePath =
            screenShotManager.captureScreenShot(
                Status.FAIL,
                result.getInstance().getClass().getSimpleName(),
                result.getMethod().getMethodName());

        String screenShotRelativePath = getRelativePathToReport(screenShotAbsolutePath);

        test.get().addScreenCaptureFromPath(screenShotRelativePath);

        TestInfo testInfo =
            (TestInfo) RunTimeContext.getInstance().getTestLevelVariables(TEST_INFO_OBJECT);
        testInfo
            .getTestRailDataHandler()
            .addStepResult(TestRailStatus.Failed, result.getThrowable().getMessage(), screenShotRelativePath);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public String getRelativePathToReport(String file) {
    Path path = new File(file).toPath();
    Path targetPath = new File(TARGET_PATH).toPath();
    return targetPath.relativize(path).toString();
  }
}
