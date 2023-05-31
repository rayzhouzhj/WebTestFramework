package com.github.test.demo.pom;

import com.scmp.framework.context.RunTimeContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.CacheLookup;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class GoogleHome extends BasePage {
	public final String URL;

	@CacheLookup
	@FindBy(css = "textarea[type=\"search\"]")
	public WebElement SearchInputBox;

	@CacheLookup
	@FindBy(css = "input[name='btnK']")
	public WebElement SearchButton;

	@CacheLookup
	@FindBy(css = "img[alt='Google']")
	public WebElement GoogleLogo;

	@CacheLookup
	@FindBy(css = "div#SIvCob")
	public WebElement GoogleLanguageSupport;

	public GoogleHome(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
		URL = runTimeContext.getURL();
	}

	@Override
	public boolean waitForPageLoad() {
		return this.waitForElementToBeClickable(GoogleLanguageSupport, 1);
	}

	@Override
	public void launch() {
		driver.get(URL);
	}
}
