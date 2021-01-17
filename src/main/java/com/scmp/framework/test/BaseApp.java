package com.scmp.framework.test;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.html5.LocalStorage;
import org.openqa.selenium.html5.WebStorage;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;

public abstract class BaseApp {
    private RemoteWebDriver driver;

    public BaseApp(RemoteWebDriver driver) {
        this.driver = driver;
    }

    public RemoteWebDriver getDriver() {
        return this.driver;
    }

    protected void initApp() {
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (BasePage.class.isAssignableFrom(field.getType())) {
                try {
                    field.set(this, field.getType().getConstructors()[0].newInstance(driver));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void refresh(){
        this.getDriver().navigate().refresh();
    }

    public void navigateTo(String path){
        this.getDriver().navigate().to(this.getURL() + path);
    }

    public String getURL() {
        String url = this.driver.getCurrentUrl().replace("?action=preview", "");
        if(url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }
        return url;
    }

    public void mouseClick(WebElement element) {
        new Actions(this.getDriver()).moveToElement(element).click().perform();
    }

    public void javascriptClick(WebElement element) {
        this.getDriver().executeScript("arguments[0].click();", element);
    }

    public void mouseFocus(WebElement element) {
        new Actions(this.getDriver()).moveToElement(element).perform();
    }

    public void executeScript(String script, String... arguments) {
        this.getDriver().executeScript(script, arguments);
    }

    public void switchToTab(int tabSequence) {
        ArrayList<String> tabs = new ArrayList<> (driver.getWindowHandles());
        driver.switchTo().window(tabs.get(tabSequence));
    }

    public void setLocalStorage(Map<String, String> dataMap) {
        LocalStorage local = ((WebStorage) this.driver).getLocalStorage();
        for(String key : dataMap.keySet()) {
            local.setItem(key, dataMap.get(key));
        }
    }

    public String getLocalStorage(String key) {
        LocalStorage local = ((WebStorage) this.driver).getLocalStorage();
        return local.getItem(key);
    }

    public void setLocalStorage(String key, String value) {
        LocalStorage local = ((WebStorage) this.driver).getLocalStorage();
        local.setItem(key, value);
    }
}
