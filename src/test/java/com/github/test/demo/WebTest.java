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
	
	private void init(String methodName)
	{
		System.out.println(methodName);
		
		Browser = new Browser(WebDriverManager.getDriver());
		
		System.out.println(methodName + " Receive Driver, start testing");
	}
	
	@Test(groups = "DEBUG")
	public void testGoogleSearch1() throws InterruptedException
	{
		init("testGoogleSearch1");
		
		Browser.GoogleHome.launch();
		Thread.sleep(5000);  // Let the user actually see something!
		Browser.GoogleHome.SearchInputBox.sendKeys("selenium");
		Thread.sleep(5000);  // Let the user actually see something!
		Browser.GoogleHome.SearchButton.click();
		Browser.GoogleSearchResult.waitForPageLoad();
		Thread.sleep(5000);  // Let the user actually see something!
	}
	
	@Test
	public void testGoogleSearch2() throws InterruptedException 
	{
		init("testGoogleSearch2");
		
		Browser.GoogleHome.launch();
		Thread.sleep(5000);  // Let the user actually see something!
		Browser.GoogleHome.SearchInputBox.sendKeys("selenium");
		Browser.GoogleHome.SearchButton.click();
		Browser.GoogleSearchResult.waitForPageLoad();
		Thread.sleep(5000);  // Let the user actually see something!
		
		System.out.println("testGoogleSearch2 Completed");
	}
	
	@Test
	public void testGoogleSearch3() throws InterruptedException 
	{
		System.out.println("testGoogleSearch3");
		WebDriver driver = WebDriverManager.getDriver();
		System.out.println("testGoogleSearch3 Receive Driver, start testing");
		
		driver.get("https://www.google.com");
		Thread.sleep(5000);  // Let the user actually see something!
		driver.findElement(By.id("lst-ib")).sendKeys("selenium");
		Thread.sleep(5000);  // Let the user actually see something!
		
		System.out.println("testGoogleSearch3 Completed");
	}
	
	@Test
	public void testGoogleSearch4() throws InterruptedException
	{
		System.out.println("testGoogleSearch4");
		
		WebDriver driver = WebDriverManager.getDriver();
		System.out.println("testGoogleSearch4 Receive Driver, start testing");
		
		driver.get("https://www.google.com");
		Thread.sleep(5000);  // Let the user actually see something!
		driver.findElement(By.id("lst-ib")).sendKeys("selenium");
		Thread.sleep(5000);  // Let the user actually see something!
		
		System.out.println("testGoogleSearch4 Completed");
	}
	
}
