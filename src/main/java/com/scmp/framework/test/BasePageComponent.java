package com.scmp.framework.test;

import org.openqa.selenium.remote.RemoteWebDriver;

public abstract class BasePageComponent extends BasePageElement {
    private RemoteWebDriver driver;

    public BasePageComponent(RemoteWebDriver driver) {
        super(driver);
        this.driver = driver;
    }

    public abstract boolean waitForComponentLoad();
}
