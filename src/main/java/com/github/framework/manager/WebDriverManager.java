package com.github.framework.manager;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.BrowserType;
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
			// For Execution Mode
			if("OFF".equalsIgnoreCase(RunTimeContext.getInstance().getProperty("DEBUG_MODE", "OFF")))
			{
				currentDriverSession = new RemoteWebDriver(new URL(context.getProperty("HOST_URL")), browser);
			}
			// For Debug Mode, launch local driver
			else
			{
				String driverHome = RunTimeContext.getInstance().getProperty("DRIVER_HOME");

				if(browser.getBrowserName().equals(BrowserType.CHROME))
				{
					System.out.println("Launch local Chrome Browser");
					System.setProperty("webdriver.chrome.driver", driverHome + "/chromedriver");
					currentDriverSession = new ChromeDriver();
				}
				else
				{
					System.out.println("Launch local Firefox Browser");
					System.setProperty("webdriver.gecko.driver", driverHome + "/geckodriver");
					currentDriverSession = new FirefoxDriver();
				}
			}

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
