package com.scmp.framework.test;

import com.scmp.framework.report.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.testng.Assert;

@Component
public class TestLogger {

	private static final Logger frameworkLogger = LoggerFactory.getLogger(TestLogger.class);
	private final ReportService reportService;

	@Autowired
	public TestLogger(ReportService reportService) {
		this.reportService = reportService;
	}

	public String captureScreen() {
		return reportService.captureScreenShot();
	}

	public String getImagePath(String imageName) {
		return reportService.getImagePath(imageName);
	}

	public void attachImage(String image) {
		reportService.attachImage(image);
	}

	public String logScreenshot() {
		return reportService.logScreenshot();
	}

	public void logInfo(String message) {
		frameworkLogger.info(message);
		reportService.logInfo(message);
	}

	public void logInfoWithScreenshot(String message) {
		frameworkLogger.info(message);
		reportService.logInfoWithScreenshot(message);
	}

	public void logPass(String message) {
		frameworkLogger.info("[PASSED] " + message);
		reportService.logPass(message);
	}

	public void logPassWithScreenshot(String message) {
		frameworkLogger.info("[PASSED] " + message);
		reportService.logPassWithScreenshot(message);
	}

	public void logFail(String message) {
		frameworkLogger.error("[FAILED] " + message);
		reportService.logFail(message);
	}

	public void logFailWithoutScreenshot(String message) {
		frameworkLogger.error("[FAILED] " + message);
		reportService.logFailWithoutScreenshot(message);
	}

	public void logFailWithImage(String message, String imagePath) {
		frameworkLogger.error("[FAILED] " + message);
		reportService.logFailWithImage(message, imagePath);
	}

	public void logFatalError(String message) {
		frameworkLogger.error("[ERROR] " + message);
		Assert.fail(message);
	}

	public void logJson(String json, String fileName) {
		frameworkLogger.info(json);
		reportService.logJson(json, fileName);
	}
}
