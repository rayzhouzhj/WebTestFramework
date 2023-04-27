package com.scmp.framework.test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.time.Duration;

public abstract class BasePageElement {
  private static final Logger frameworkLogger = LoggerFactory.getLogger(BasePageElement.class);
  private RemoteWebDriver driver;

  public BasePageElement(RemoteWebDriver driver) {
    this.driver = driver;

    Field[] fields = this.getClass().getFields();
    for (Field field : fields) {
      if (BasePageComponent.class.isAssignableFrom(field.getType())) {
        try {
          field.set(this, field.getType().getConstructors()[0].newInstance(driver));
        } catch (Exception e) {
          frameworkLogger.error("Ops!", e);
        }
      }
    }
  }

  public void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      frameworkLogger.error("Ops!", e);
    }
  }

  public boolean waitForVisible(WebElement element) {
    return waitForVisible(element, 60);
  }

  public boolean waitForVisible(WebElement element, long seconds) {
    try {
      new WebDriverWait(driver, Duration.ofSeconds(seconds)).until(ExpectedConditions.visibilityOf(element));

      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public boolean waitForElementToBeClickable(WebElement element) {
    return waitForElementToBeClickable(element, 15);
  }

  public boolean waitForElementToBeClickable(WebElement element, long seconds) {
    try {
      new WebDriverWait(driver, Duration.ofSeconds(seconds)).until(ExpectedConditions.elementToBeClickable(element));

      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public void waitForElementToDisAppear(String id) {
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id(id)));
  }

  public boolean waitForElementToDisAppear(WebElement element) {
    try {
      new WebDriverWait(driver, Duration.ofSeconds(15)).until(ExpectedConditions.invisibilityOf(element));

      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public WebElement waitForElement(WebElement element) {
    waitForElementToLoad(element);

    return element;
  }

  public boolean isIframeLoaded(WebElement element) {
    return this.isIframeLoaded(element, 5);
  }

  public boolean isIframeLoaded(WebElement element, int secondToWait) {
    try {
      new WebDriverWait(driver, Duration.ofSeconds(secondToWait))
          .until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(element));

      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public boolean isElementDisplayed(WebElement element) {
    return waitForVisible(element, 3);
  }

  public boolean waitForElementToLoad(WebElement element) {
    return waitForElementToBeClickable(element, 15);
  }

  public boolean waitForElementToLoad(WebElement element, long secondsToWait) {
    return waitForElementToBeClickable(element, secondsToWait);
  }
}
