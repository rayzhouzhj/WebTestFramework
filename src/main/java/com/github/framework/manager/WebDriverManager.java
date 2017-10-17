package com.github.framework.manager;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.github.framework.context.RunTimeContext;

public class WebDriverManager 
{
	private static ThreadLocal<RemoteWebDriver> remoteWebDriver = new ThreadLocal<>();
	private RunTimeContext context;

	public WebDriverManager() throws Exception 
	{
		context = RunTimeContext.getInstance();
	}

	public static RemoteWebDriver getDriver()
	{
		return remoteWebDriver.get();
	}

	protected static void setDriver(RemoteWebDriver driver) 
	{
		remoteWebDriver.set(driver);
	}

	public void startDriverInstance(DesiredCapabilities browser) 
	{
		RemoteWebDriver currentDriverSession;
		try 
		{
			currentDriverSession = new RemoteWebDriver(new URL(context.getProperty("HOST_URL")), browser);
			
			currentDriverSession.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
			WebDriverManager.setDriver(currentDriverSession);
		}
		catch (MalformedURLException e) 
		{
			e.printStackTrace();
		}
	}

	public void stopWebDriver() 
	{
		WebDriverManager.getDriver().quit();
	}

}
