package com.github.framework.utils;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import com.aventstack.extentreports.Status;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.ITestResult;

import com.github.framework.context.RunTimeContext;
import com.github.framework.manager.WebDriverManager;

/**
 *
 */
public class ScreenShotManager 
{

//    private String screenShotNameWithTimeStamp;

    public ScreenShotManager() 
    {
    }

    public String captureScreenShot(Status status, String className, String methodName) throws IOException, InterruptedException
    {
    	// If driver is not setup properly
    	if(WebDriverManager.getDriver() == null)
    	{
    		return "";
    	}
    	
        File scrFile = ((TakesScreenshot) WebDriverManager.getDriver()).getScreenshotAs(OutputType.FILE);
        String screenShotNameWithTimeStamp = currentDateAndTime();
        
        return copyscreenShotToTarget(status, scrFile, methodName, className, screenShotNameWithTimeStamp);
    }

    public String captureScreenShot(String screenShotName) throws InterruptedException, IOException
    {
        String className = new Exception().getStackTrace()[1].getClassName();
        
        return captureScreenShot(Status.INFO, className, screenShotName);
    }

    private String currentDateAndTime()
    {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dtf = DateTimeFormatter.ISO_DATE_TIME;
        return now.truncatedTo(ChronoUnit.SECONDS).format(dtf).replace(":", "-");
    }

    private String copyscreenShotToTarget(Status status,
                                    File scrFile, String methodName,
                                    String className, String screenShotNameWithTimeStamp) 
    {
    	String filePath = RunTimeContext.getInstance().getLogPath("screenshot", className, methodName);

        try 
        {
            if (status == Status.FAIL)
            {
                String failedScreen = filePath + File.separator + screenShotNameWithTimeStamp + "_" + methodName + "_failed.png";
                FileUtils.copyFile(scrFile, new File(failedScreen.trim()));
                
                return failedScreen.trim();
            } 
            else
            {
                String capturedScreen = filePath + File.separator  + screenShotNameWithTimeStamp + "_" + methodName + "_results.png";
                FileUtils.copyFile(scrFile, new File(capturedScreen.trim()));
                
                return capturedScreen.trim();
            }
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
        
        return "";
    }

}
