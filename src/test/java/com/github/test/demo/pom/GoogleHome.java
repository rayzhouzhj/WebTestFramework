package com.github.test.demo.pom;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.CacheLookup;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class GoogleHome extends BasePage
{
	public static final String URL = "https://www.google.com";
	@CacheLookup
	@FindBy(id = "lst-ib")
	public WebElement SearchInputBox;
	
	public GoogleHome(RemoteWebDriver driver) 
	{
		super(driver);
		PageFactory.initElements(driver, this);
	}

	@Override
	public boolean waitForPageLoad() 
	{
		return this.waitForVisibility(SearchInputBox, 1);
	}

	@Override
	public void launch() 
	{
		driver.get(URL);
	}
}
