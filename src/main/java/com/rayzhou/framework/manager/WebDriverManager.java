package com.rayzhou.framework.manager;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.rayzhou.framework.context.RunTimeContext;

public class WebDriverManager {
    private static ThreadLocal<RemoteWebDriver> remoteWebDriver = new ThreadLocal<>();
    private RunTimeContext context;

    public WebDriverManager() {
        context = RunTimeContext.getInstance();
    }

    public static RemoteWebDriver getDriver() {
        return remoteWebDriver.get();
    }

    protected static void setDriver(RemoteWebDriver driver) {
        remoteWebDriver.set(driver);
    }

    public void startDriverInstance(MutableCapabilities browser, Dimension screenDimension) throws Exception {
        RemoteWebDriver currentDriverSession;

        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.BROWSER, Level.INFO);
        browser.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);

        // For Execution Mode
        if (!RunTimeContext.getInstance().isDebugMode()) {
            currentDriverSession = new RemoteWebDriver(new URL(context.getProperty("HOST_URL")), browser);
        }
        // For Debug Mode, launch local driver
        else {
            String driverHome = RunTimeContext.getInstance().getProperty("DRIVER_HOME");

            if (browser.getBrowserName().equals(BrowserType.CHROME)) {
                System.out.println("Launch local Chrome Browser");
                System.setProperty("webdriver.chrome.driver", driverHome + "/chromedriver");
                currentDriverSession = new ChromeDriver((ChromeOptions) browser);
            } else {
                System.out.println("Launch local Firefox Browser");
                System.setProperty("webdriver.gecko.driver", driverHome + "/geckodriver");
                currentDriverSession = new FirefoxDriver((FirefoxOptions) browser);
            }
        }

        currentDriverSession.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        WebDriverManager.setDriver(currentDriverSession);

        // Set screen dimension
        currentDriverSession.manage().window().setSize(screenDimension);
    }

    public void stopWebDriver() {
        if(WebDriverManager.getDriver() != null) {
            WebDriverManager.getDriver().quit();
        }
    }

}
