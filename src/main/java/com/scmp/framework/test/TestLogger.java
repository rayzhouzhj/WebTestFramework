package com.scmp.framework.test;

import com.scmp.framework.report.ReportManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.testng.Assert;

@Component
public class TestLogger {

	private static final Logger frameworkLogger = LoggerFactory.getLogger(TestLogger.class);
	private final ReportManager reportManager;

	@Autowired
	public TestLogger(ReportManager reportManager) {
		this.reportManager = reportManager;
	}

	public String captureScreen() {
		return reportManager.captureScreenShot();
	}

	public String getImagePath(String imageName) {
		return reportManager.getImagePath(imageName);
	}

	public void attachImage(String image) {
		reportManager.attachImage(image);
	}

	public String logScreenshot() {
		return reportManager.logScreenshot();
	}

	public void logInfo(String message) {
		frameworkLogger.info(message);
		reportManager.logInfo(message);
	}

	public void logInfoWithScreenshot(String message) {
		frameworkLogger.info(message);
		reportManager.logInfoWithScreenshot(message);
	}

	public void logPass(String message) {
		frameworkLogger.info("[PASSED] " + message);
		reportManager.logPass(message);
	}

	public void logPassWithScreenshot(String message) {
		frameworkLogger.info("[PASSED] " + message);
		reportManager.logPassWithScreenshot(message);
	}

	public void logFail(String message) {
		frameworkLogger.error("[FAILED] " + message);
		reportManager.logFail(message);
	}

	public void logFailWithoutScreenshot(String message) {
		frameworkLogger.error("[FAILED] " + message);
		reportManager.logFailWithoutScreenshot(message);
	}

	public void logFailWithImage(String message, String imagePath) {
		frameworkLogger.error("[FAILED] " + message);
		reportManager.logFailWithImage(message, imagePath);
	}

	public void logFatalError(String message) {
		frameworkLogger.error("[ERROR] " + message);
		Assert.fail(message);
	}

	public void logJson(String json, String fileName) {
		frameworkLogger.info(json);
		reportManager.logJson(json, fileName);
	}
}
