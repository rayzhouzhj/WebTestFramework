package com.github.test.demo.pom;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.github.framework.report.TestLogManager;

public abstract class BasePage 
{
	public RemoteWebDriver driver;
	
	public BasePage(RemoteWebDriver driver) 
	{
		this.driver = driver;
	}
	
	public abstract void launch();
	
	public void sleep(long millis)
	{
		try 
		{
			Thread.sleep(millis);
		}
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
	}
	
    public abstract boolean waitForPageLoad();
    
    public boolean waitForVisibility(WebElement element)
    {
    	return waitForVisibility(element, 60);
    }
    
    public boolean waitForVisibility(WebElement element, long seconds)
    {
    	try
    	{
           new WebDriverWait(driver, seconds).until(ExpectedConditions.visibilityOf(element));
           
           return true;
    	}
    	catch(Exception e)
    	{
    		return false;
    	}
    }
    
    public boolean waitForPageToLoad(WebElement element) 
    {
        return waitForPageToLoad(element, 15);
    }

    public boolean waitForPageToLoad(WebElement element, int secondsToWait) 
    {
        try
    	{
        	WebDriverWait wait = new WebDriverWait(driver, secondsToWait);
            wait.until(ExpectedConditions.elementToBeClickable(element));
           
           return true;
    	}
    	catch(Exception e)
    	{
    		return false;
    	}
        
    }
    
    public void waitForElementToDisAppear(String id) 
    {
        WebDriverWait wait = new WebDriverWait(driver, 15);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id(id)));
    }

    public WebElement waitForElement(WebElement arg) 
    {
        waitForPageToLoad(arg);
        WebElement el = arg;
        
        return el;
    }

    public boolean isElementPresent(By by) 
    {
        try 
        {
            driver.findElement(by);
            
            return true;
        } 
        catch (NoSuchElementException e) 
        {
            return false;
        }

    }
}
