package com.github.test.demo;

import java.net.MalformedURLException;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

import com.github.framework.manager.WebDriverManager;
import com.github.test.demo.pom.Browser;

public class WebTest 
{
	Browser Browser;
	
	private void init()
	{
		Browser = new Browser(WebDriverManager.getDriver());
	}
	
	@Test
	public void testGoogleSearch1() throws InterruptedException, MalformedURLException {
		// Optional, if not specified, WebDriver will search your path for chromedriver.
		//	  System.setProperty("webdriver.chrome.driver", "/Users/ray_zhou/Documents/WebDriver/chromedriver");
		System.out.println("testGoogleSearch1");
		init();
		System.out.println("testGoogleSearch1 Receive Driver, start testing");
		
		Browser.GoogleHome.launch();
		Thread.sleep(5000);  // Let the user actually see something!
		Browser.GoogleHome.SearchInputBox.sendKeys("selenium");
		Thread.sleep(5000);  // Let the user actually see something!
		
		System.out.println("testGoogleSearch1 Completed");
	}
	
	@Test
	public void testGoogleSearch2() throws InterruptedException, MalformedURLException {
		System.out.println("testGoogleSearch2");
		init();
		System.out.println("testGoogleSearch2 Receive Driver, start testing");
		
		Browser.GoogleHome.launch();
		Thread.sleep(5000);  // Let the user actually see something!
		Browser.GoogleHome.SearchInputBox.sendKeys("selenium");
		Thread.sleep(5000);  // Let the user actually see something!
		
		System.out.println("testGoogleSearch2 Completed");
	}
	
	@Test
	public void testGoogleSearch3() throws InterruptedException, MalformedURLException {
		// Optional, if not specified, WebDriver will search your path for chromedriver.
		//	  System.setProperty("webdriver.chrome.driver", "/Users/ray_zhou/Documents/WebDriver/chromedriver");
		System.out.println("testGoogleSearch3");
		
//		WebDriver driver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), DesiredCapabilities.firefox());
		WebDriver driver = WebDriverManager.getDriver();
		System.out.println("testGoogleSearch3 Receive Driver, start testing");
		
		//	  WebDriver driver = new ChromeDriver();
		driver.get("https://www.google.com");
		Thread.sleep(5000);  // Let the user actually see something!
		driver.findElement(By.id("lst-ib")).sendKeys("selenium");
		Thread.sleep(5000);  // Let the user actually see something!
		driver.quit();
		
		System.out.println("testGoogleSearch3 Completed");
	}
	
	@Test
	public void testGoogleSearch4() throws InterruptedException, MalformedURLException {
		// Optional, if not specified, WebDriver will search your path for chromedriver.
		//	  System.setProperty("webdriver.chrome.driver", "/Users/ray_zhou/Documents/WebDriver/chromedriver");
		System.out.println("testGoogleSearch4");
		
//		WebDriver driver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), DesiredCapabilities.chrome());
		WebDriver driver = WebDriverManager.getDriver();
		System.out.println("testGoogleSearch4 Receive Driver, start testing");
		
		//	  WebDriver driver = new ChromeDriver();
		driver.get("https://www.google.com");
		Thread.sleep(5000);  // Let the user actually see something!
		driver.findElement(By.id("lst-ib")).sendKeys("selenium");
		Thread.sleep(5000);  // Let the user actually see something!
		driver.quit();
		
		System.out.println("testGoogleSearch4 Completed");
	}
	
}
