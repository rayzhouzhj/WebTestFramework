package com.github.test.demo;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

import com.github.framework.manager.WebDriverManager;
import com.github.test.demo.pom.Browser;

public class WebTest 
{
	Browser Browser;
	
	private void init(String methodName)
	{
		System.out.println(methodName);
		
		Browser = new Browser(WebDriverManager.getDriver());
		
		System.out.println(methodName + " Receive Driver, start testing");
	}
	
	@Test(groups = "DEBUG")
	public void testGoogleSearchPassCase1() throws InterruptedException
	{
		init("testGoogleSearch1");
		
		Browser.GoogleHome.launch();
		Thread.sleep(2000);  // Let the user actually see something!
		Browser.GoogleHome.SearchInputBox.sendKeys("selenium");
		Browser.GoogleHome.GoogleLogo.click(); // Dismiss fast forward search
		Thread.sleep(2000);  // Let the user actually see something!
		Browser.GoogleHome.SearchButton.click();
		Browser.GoogleSearchResult.waitForPageLoad();
		Thread.sleep(2000);  // Let the user actually see something!
	}
	
	@Test(groups = "DEBUG")
	public void testGoogleSearchPassCase2() throws InterruptedException 
	{
		init("testGoogleSearch2");
		
		Browser.GoogleHome.launch();
		Thread.sleep(2000);  // Let the user actually see something!
		Browser.GoogleHome.SearchInputBox.sendKeys("selenium");
		Browser.GoogleHome.GoogleLogo.click(); // Dismiss fast forward search
		Browser.GoogleHome.SearchButton.click();
		Browser.GoogleSearchResult.waitForPageLoad();
		Thread.sleep(2000);  // Let the user actually see something!
		
		System.out.println("testGoogleSearch2 Completed");
	}
	
	@Test(groups = "DEBUG")
	public void testGoogleSearchFailCase1() throws InterruptedException 
	{
		System.out.println("testGoogleSearchFailCase1");
		WebDriver driver = WebDriverManager.getDriver();
		System.out.println("testGoogleSearch3 Receive Driver, start testing");
		
		driver.get("https://www.yahoo.com");
		Thread.sleep(2000);  // Let the user actually see something!
		driver.findElement(By.id("lst-ib")).sendKeys("selenium");
		Thread.sleep(2000);  // Let the user actually see something!
		
		System.out.println("testGoogleSearchFailCase1 Completed");
	}
	
	@Test(groups = "DEBUG")
	public void testGoogleSearchFailCase2() throws InterruptedException
	{
		System.out.println("testGoogleSearchFailCase2");
		
		WebDriver driver = WebDriverManager.getDriver();
		System.out.println("testGoogleSearch4 Receive Driver, start testing");
		
		driver.get("https://www.facebook.com");
		Thread.sleep(2000);  // Let the user actually see something!
		driver.findElement(By.id("lst-ib")).sendKeys("selenium");
		Thread.sleep(2000);  // Let the user actually see something!
		
		System.out.println("testGoogleSearchFailCase2 Completed");
	}
	
}
