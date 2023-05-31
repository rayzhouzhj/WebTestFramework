package com.scmp.framework.report;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import com.scmp.framework.context.RunTimeContext;
import com.scmp.framework.testng.listeners.RetryAnalyzer;
import com.scmp.framework.testng.model.TestInfo;
import com.scmp.framework.testrail.TestRailStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.scmp.framework.utils.ScreenShotManager;

import static com.scmp.framework.utils.Constants.TARGET_PATH;

/**
 * ReportManager - Handles all Reporting activities e.g. communication with ExtentManager, etc
 */
@Component
public class ReportManager {
	private static final Logger frameworkLogger = LoggerFactory.getLogger(ReportManager.class);
	private final ThreadLocal<TestInfo> testInfo = new ThreadLocal<>();
	private final ThreadLocal<ExtentTest> parentTestClass = new ThreadLocal<>();
	private final ThreadLocal<ExtentTest> currentTestMethod = new ThreadLocal<>();
	private final ThreadLocal<ITestResult> testResult = new ThreadLocal<>();
	private final ThreadLocal<Boolean> setupStatus = new ThreadLocal<>();
	private final ScreenShotManager screenshotManager;
	private final RunTimeContext runTimeContext;
	private final ExtentTestService extentTestService;

	@Autowired
	private ReportManager(RunTimeContext runTimeContext, ExtentTestService extentTestService, ScreenShotManager screenshotManager) {
		this.runTimeContext = runTimeContext;
		this.screenshotManager = screenshotManager;
		this.extentTestService = extentTestService;
	}

	public void removeTest() {
		extentTestService.removeTest(currentTestMethod.get());
	}

	public void skipTest() {
		currentTestMethod.get().getModel().setStatus(Status.SKIP);
	}

	/**
	 * Log details for failure case
	 * 1. Print stack trace
	 * 2. Capture screenshot
	 *
	 * @param result TestNG test result
	 */
	private void handleTestFailure(ITestResult result) {

		if (result.getStatus()==ITestResult.FAILURE) {
			// Print exception stack trace if any
			Throwable throwable = result.getThrowable();
			if (throwable!=null) {
				frameworkLogger.error("Ops!", throwable);
				currentTestMethod.get().log(Status.FAIL, "<pre>" + result.getThrowable().getMessage() + "</pre>");
				this.addTestRailLog(TestRailStatus.Failed, result.getThrowable().getMessage(), null);
			}

			// Add screenshot
			try {
				String screenShotAbsolutePath =
						screenshotManager.captureScreenShot(
								Status.FAIL,
								result.getInstance().getClass().getSimpleName(),
								result.getMethod().getMethodName());

				String screenShotRelativePath = getRelativePathToReport(screenShotAbsolutePath);
				currentTestMethod.get().addScreenCaptureFromPath(screenShotRelativePath);
				this.addTestRailLog(TestRailStatus.Failed, "", screenShotAbsolutePath);

			} catch (Exception e) {
				frameworkLogger.error("Ops!", e);
			}
		}
	}

	public void endLogTestResults(ITestResult result) {

		this.testInfo.get().setTestEndTime();
		if (result.isSuccess()) {
			String message = "Test Passed: " + result.getMethod().getMethodName();
			currentTestMethod.get().log(Status.PASS, message);
			this.addTestRailLog(TestRailStatus.Passed, message, null);

		} else {
			if (result.getStatus()==ITestResult.FAILURE) {
				handleTestFailure(result);
			}
		}

		if (result.getStatus()==ITestResult.SKIP) {
			currentTestMethod.get().log(Status.SKIP, "Test skipped");
		}

		extentTestService.flush();


		// Handling for Retry
		IRetryAnalyzer analyzer = result.getMethod().getRetryAnalyzer();
		if (analyzer instanceof RetryAnalyzer) {
			if (((RetryAnalyzer) analyzer).isRetriedMethod(result) ||
					result.getStatus()==ITestResult.FAILURE) {
				this.addTag("RETRIED");
			}

			if (runTimeContext.getFrameworkConfigs().isRemoveFailedTestB4Retry()
					&& result.getStatus()==ITestResult.FAILURE
					&& ((RetryAnalyzer) analyzer).isRetriedRequired(result)) {
				this.removeTest();
			}
		}

		if (result.getStatus()==ITestResult.SKIP) {
			this.skipTest();
		}

		extentTestService.flush();

		this.testInfo.get().uploadTestResultsToTestRail();
	}

	public void setTestResult(ITestResult testResult) {
		this.testResult.set(testResult);
	}

	public void setSetupStatus(boolean status) {
		this.setupStatus.set(status);
	}

	public boolean getSetupStatus() {
		return this.setupStatus.get()!=null && this.setupStatus.get();
	}

	public synchronized void setupReportForTestSet(TestInfo testInfo) {
		ExtentTest parent = extentTestService.createTest(testInfo.getClassName(), testInfo.getClassDescription());
		if (testInfo.getClassLevelGroups()!=null) {
			parent.assignCategory(testInfo.getClassLevelGroups());
		}

		parentTestClass.set(parent);

	}

	public synchronized void setTestInfo(TestInfo testInfo) {

		this.testInfo.set(testInfo);

		ExtentTest child = parentTestClass.get().createNode(testInfo.getTestName(), testInfo.getTestMethodDescription());
		currentTestMethod.set(child);

		// Update authors
		if (testInfo.getAuthorNames()!=null) {
			currentTestMethod.get().assignAuthor(testInfo.getAuthorNames());
		}

		// Update groups to category
		currentTestMethod.get().assignCategory(testInfo.getTestGroups());
		// Added browser type tag to test
		currentTestMethod.get().assignCategory(testInfo.getBrowserType().toString());
	}

	public void addTag(String tag) {
		this.currentTestMethod.get().assignCategory(tag);
	}

	public String getImagePath(String imageName) {
		String[] classAndMethod = getTestClassNameAndMethodName().split(",");
		try {
			return screenshotManager.getScreenshotPath(classAndMethod[0], classAndMethod[1], imageName);
		} catch (Exception e) {
			frameworkLogger.error("Ops!", e);
			return null;
		}
	}

	private void addTestRailLog(int status, String message, String imagePath) {
		this.testInfo.get().addTestResultForTestRail(status, message, imagePath);
	}

	public void logInfo(String message) {
		this.currentTestMethod.get().log(Status.INFO, message);
		this.addTestRailLog(TestRailStatus.Passed, message, null);
	}

	public String logScreenshot() {
		String imagePath = this.logScreenshot(Status.INFO);
		this.addTestRailLog(TestRailStatus.Passed, "", imagePath);

		return imagePath;
	}

	private String logScreenshot(Status status) {
		try {
			String[] classAndMethod = getTestClassNameAndMethodName().split(",");
			String screenShotAbsolutePath = screenshotManager.captureScreenShot(Status.INFO, classAndMethod[0], classAndMethod[1]);
			String screenShotRelativePath = getRelativePathToReport(screenShotAbsolutePath);
			this.currentTestMethod.get().log(status,
					"<img data-featherlight=" + screenShotRelativePath + " width=\"10%\" src=" + screenShotRelativePath + " data-src=" + screenShotRelativePath + ">");

			return screenShotAbsolutePath;
		} catch (Exception e) {
			frameworkLogger.error("Ops!", e);
		}

		return "";
	}

	public void logInfoWithScreenshot(String message) {
		this.currentTestMethod.get().log(Status.INFO, message);
		String imagePath = this.logScreenshot();

		this.addTestRailLog(TestRailStatus.Passed, message, imagePath);
	}

	public void logPass(String message) {
		this.currentTestMethod.get().log(Status.PASS, message);
		this.addTestRailLog(TestRailStatus.Passed, message, null);
	}

	public void logPassWithScreenshot(String message) {
		this.currentTestMethod.get().log(Status.PASS, message);
		String imagePath = this.logScreenshot(Status.PASS);

		this.addTestRailLog(TestRailStatus.Passed, message, imagePath);
	}

	public void logFail(String message) {
		this.testResult.get().setStatus(ITestResult.FAILURE);
		this.currentTestMethod.get().log(Status.FAIL, message);
		String imagePath = this.logScreenshot(Status.FAIL);

		this.addTestRailLog(TestRailStatus.Failed, message, imagePath);
	}

	public void logFailWithoutScreenshot(String message) {
		this.currentTestMethod.get().log(Status.FAIL, message);
		this.testResult.get().setStatus(ITestResult.FAILURE);

		this.addTestRailLog(TestRailStatus.Failed, message, null);
	}

	public void logFailWithImage(String message, String originalImagePath) {
		String imageRelativePath = getRelativePathToReport(originalImagePath);
		try {
			this.currentTestMethod.get().log(Status.FAIL, message);
			this.currentTestMethod.get().log(Status.FAIL,
					"<img data-featherlight=" + imageRelativePath + " width=\"10%\" src=" + imageRelativePath + " data-src=" + imageRelativePath + ">");
			this.testResult.get().setStatus(ITestResult.FAILURE);

			this.addTestRailLog(TestRailStatus.Failed, message, originalImagePath);
		} catch (Exception e) {
			frameworkLogger.error("Ops!", e);
		}
	}

	public void logJson(String json, String fileName) {
		StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
		int classIndex = 0;
		for (int i = 0; i < stElements.length; i++) {
			if (stElements[i].getClassName().equals("sun.reflect.NativeMethodAccessorImpl")) {
				classIndex = i - 1;
				break;
			}
		}
		String filePath = runTimeContext.getLogPath("json", stElements[classIndex].getClassName(), stElements[classIndex].getMethodName());
		if (fileName==null) {
			filePath = filePath + File.separator + RunTimeContext.currentDateAndTime() + ".json";
		} else {
			filePath = filePath + File.separator + fileName + ".json";
		}

		try (FileWriter fw = new FileWriter(filePath)) {
			fw.write(json);
			fw.flush();

			this.logInfo("<a target='_blank' href='" + getRelativePathToReport(filePath) + "'> " + fileName + " </a>");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String captureScreenShot() {
		try {
			String[] classAndMethod = getTestClassNameAndMethodName().split(",");
			return screenshotManager.captureScreenShot(Status.INFO, classAndMethod[0], classAndMethod[1]);
		} catch (Exception e) {
			frameworkLogger.error("Ops!", e);
		}

		return null;
	}

	public void attachImage(String imagePath) {
		try {
			this.currentTestMethod.get().addScreenCaptureFromPath(imagePath);
			this.addTestRailLog(TestRailStatus.Passed, "", imagePath);
		} catch (Exception e) {
			frameworkLogger.error("Ops!", e);
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
