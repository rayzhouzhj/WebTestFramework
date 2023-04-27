package com.github.test.demo;

import com.scmp.framework.annotations.Authors;
import com.scmp.framework.annotations.screens.Device;
import com.scmp.framework.test.BaseTest;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

import com.scmp.framework.annotations.ChromeArguments;
import com.scmp.framework.annotations.HeadlessMode;
import com.scmp.framework.annotations.screens.DeviceName;
import com.scmp.framework.manager.WebDriverManager;
import com.github.test.demo.pom.Browser;

public class WebTest extends BaseTest {
    Browser Browser;
    static int value = 1;
    private void init(String methodName) {
        System.out.println(methodName);

        // Init a browser instance using webdriver
        Browser = new Browser(WebDriverManager.getDriver());

        logger.logInfo(methodName + " Receive Driver, start testing");
    }

    @Test(groups = {"RETRY"}, description = "Test Description")
    @Authors(name = {"Ray", "Ray Zhou"})
    public void testRetry1() {
        assert 1 == 1;
    }

    @Test(groups = "RETRY", description = "Test Description", dependsOnMethods = "testRetry1")
    @Authors(name = "Ray")
    public void testRetry2() {
        assert 2 == value++;
    }

    @Device(width = 768, height = 1024)
    @HeadlessMode
    @ChromeArguments(options = {"--incognito"})
    @Test(groups = "DEBUG")
    public void testGoogleSearchPassCase1() throws InterruptedException {
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

    @Device(device = DeviceName.iPhoneX)
    @Test(groups = "DEBUG")
    public void testGoogleSearchPassCase2() throws InterruptedException {
        init("testGoogleSearch2");

        Browser.GoogleHome.launch();
        Thread.sleep(2000);  // Let the user actually see something!
        Browser.GoogleHome.SearchInputBox.sendKeys("selenium");
        Browser.GoogleHome.GoogleLogo.click(); // Dismiss fast forward search
        Browser.GoogleHome.SearchButton.click();
        Browser.GoogleSearchResult.waitForPageLoad();
        Thread.sleep(2000);  // Let the user actually see something!

        logger.logInfo("testGoogleSearch2 Completed");
    }

    @Test(groups = "DEBUG")
    public void testGoogleSearchFailCase1() throws InterruptedException {
        System.out.println("testGoogleSearchFailCase1");
        WebDriver driver = WebDriverManager.getDriver();
        System.out.println("testGoogleSearch3 Receive Driver, start testing");

        driver.get("https://www.yahoo.com");
        Thread.sleep(2000);  // Let the user actually see something!
        driver.findElement(By.id("lst-ib")).sendKeys("selenium");
        Thread.sleep(2000);  // Let the user actually see something!

        logger.logInfo("testGoogleSearchFailCase1 Completed");
    }

    @Test(groups = "DEBUG")
    public void testGoogleSearchFailCase2() throws InterruptedException {
        System.out.println("testGoogleSearchFailCase2");

        WebDriver driver = WebDriverManager.getDriver();
        System.out.println("testGoogleSearch4 Receive Driver, start testing");

        driver.get("https://www.facebook.com");
        Thread.sleep(2000);  // Let the user actually see something!
        driver.findElement(By.id("lst-ib")).sendKeys("selenium");
        Thread.sleep(2000);  // Let the user actually see something!

        logger.logInfo("testGoogleSearchFailCase2 Completed");
    }

}
