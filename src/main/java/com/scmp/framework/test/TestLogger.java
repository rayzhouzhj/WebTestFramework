package com.scmp.framework.test;

import com.scmp.framework.context.RunTimeContext;
import com.scmp.framework.report.ReportManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TestLogger {

	private static final Logger frameworkLogger = LoggerFactory.getLogger(TestLogger.class);

	public String captureScreen() {
		return ReportManager.getInstance().captureScreenShot();
	}

	public String getImagePath(String imageName) {
		return ReportManager.getInstance().getImagePath(imageName);
	}

	public void attachImage(String image) {
		ReportManager.getInstance().attachImage(image);
	}

	public String logScreenshot() {
		return ReportManager.getInstance().logScreenshot();
	}

	public void logInfo(String message) {
		frameworkLogger.info(message);
		ReportManager.getInstance().logInfo(message);
	}

	public void logInfoWithScreenshot(String message) {
		frameworkLogger.info(message);
		ReportManager.getInstance().logInfoWithScreenshot(message);
	}

	public void logPass(String message) {
		frameworkLogger.info("[PASSED] " + message);
		ReportManager.getInstance().logPass(message);
	}

	public void logPassWithScreenshot(String message) {
		frameworkLogger.info("[PASSED] " + message);
		ReportManager.getInstance().logPassWithScreenshot(message);
	}

	public void logFail(String message) {
		frameworkLogger.error("[FAILED] " + message);
		ReportManager.getInstance().logFail(message);
	}

	public void logFailWithoutScreenshot(String message) {
		frameworkLogger.error("[FAILED] " + message);
		ReportManager.getInstance().logFailWithoutScreenshot(message);
	}

	public void logFailWithImage(String message, String imagePath) {
		frameworkLogger.error("[FAILED] " + message);
		ReportManager.getInstance().logFailWithImage(message, imagePath);
	}

	public void logFatalError(String message) {
		frameworkLogger.error("[ERROR] " + message);
		Assert.fail(message);
	}

	public void logJson(String json, String fileName) {
		frameworkLogger.info(json);
		ReportManager.getInstance().logJson(json, fileName);
	}
}
