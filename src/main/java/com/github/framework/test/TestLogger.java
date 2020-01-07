package com.github.framework.test;

import com.github.framework.report.ReportManager;
import org.testng.Assert;

public class TestLogger {

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
        System.out.println("[INFO] " + message);
        ReportManager.getInstance().logInfo(message);
    }

    public void logInfoWithScreenshot(String message) {
        System.out.println("[INFO] " + message);
        ReportManager.getInstance().logInfoWithScreenshot(message);
    }

    public void logPass(String message) {
        System.out.println("[PASSED] " + message);
        ReportManager.getInstance().logPass(message);
    }

    public void logPassWithScreenshot(String message) {
        System.out.println("[PASSED] " + message);
        ReportManager.getInstance().logPassWithScreenshot(message);
    }

    public void logFail(String message) {
        System.err.println("[FAILED] " + message);
        ReportManager.getInstance().logFail(message);
    }

    public void logFailWithoutScreenshot(String message) {
        System.err.println("[FAILED] " + message);
        ReportManager.getInstance().logFailWithoutScreenshot(message);
    }

    public void logFailWithImage(String message, String imagePath) {
        System.err.println("[FAILED] " + message);
        ReportManager.getInstance().logFailWithImage(message, imagePath);
    }

    public void logFatalError(String message) {
        System.err.println("[ERROR] " + message);
        Assert.fail(message);
    }
}
