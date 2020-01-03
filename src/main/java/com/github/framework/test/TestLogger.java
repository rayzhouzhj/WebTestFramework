package com.github.framework.test;

import com.github.framework.report.ReportManager;
import org.testng.Assert;

import java.io.IOException;

public class TestLogger {

    public String captureScreen(String snapshotName) throws IOException, InterruptedException {
        return ReportManager.getInstance().ScreenshotManager.captureScreenShot(snapshotName);
    }

    public void logInfo(String message)
    {
        System.out.println("[INFO] " + message);
        ReportManager.getInstance().logInfo(message);
    }

    public void logInfoWithScreenShot(String message)
    {
        System.out.println("[INFO] " + message);
        ReportManager.getInstance().logInfoWithScreenShot(message);
    }

    public void logPass(String message)
    {
        System.out.println("[PASSED] " + message);
        ReportManager.getInstance().logPass(message);
    }

    public void logFail(String message)
    {
        System.err.println("[FAILED] " + message);
        ReportManager.getInstance().logFail(message);
    }

    public void logFatalError(String message)
    {
        System.err.println("[ERROR] " + message);
        Assert.fail(message);
    }
}
