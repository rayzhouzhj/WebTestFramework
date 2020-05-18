package com.rayzhou.framework.testng.model;

import com.rayzhou.framework.annotations.*;
import com.rayzhou.framework.annotations.screens.Device;
import com.rayzhou.framework.annotations.screens.DeviceName;
import com.rayzhou.framework.model.Browser;
import com.rayzhou.framework.testng.listeners.RetryAnalyzer;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.testng.IInvokedMethod;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.logging.Level;

public class TestInfo {
    private IInvokedMethod invokedMethod;
    private ITestResult testResult;
    private Method declaredMethod;

    private Browser browserType = null;

    public TestInfo(IInvokedMethod methodName, ITestResult testResult) {
        this.invokedMethod = methodName;
        this.testResult = testResult;

        this.declaredMethod = this.invokedMethod.getTestMethod().getConstructorOrMethod().getMethod();
    }

    public ITestResult getTestResult() {
        return this.testResult;
    }

    public IInvokedMethod getInvokedMethod() {
        return this.invokedMethod;
    }

    public Method getDeclaredMethod() {
        return this.declaredMethod;
    }

    public String getClassName() {
        return this.declaredMethod.getDeclaringClass().getSimpleName();
    }

    public String[] getClassGroups() {
        ClassGroups groups = this.declaredMethod.getDeclaringClass().getAnnotation(ClassGroups.class);
        return groups == null ? null : groups.groups();
    }

    public String getClassDescription() {
        ClassDescription description = this.declaredMethod.getDeclaringClass().getAnnotation(ClassDescription.class);
        return description == null ? "" : description.value();
    }

    public String getMethodName() {
        return this.declaredMethod.getName();
    }

    public boolean isTestMethod() {
        return this.declaredMethod.getAnnotation(Test.class) != null;
    }

    public String[] getAuthorNames() {
        return declaredMethod.getAnnotation(Authors.class) == null ? null : declaredMethod.getAnnotation(Authors.class).name();
    }

    public String getTestName() {
        String dataProvider = null;
        Object dataParameter = this.invokedMethod.getTestResult().getParameters();
        if (((Object[]) dataParameter).length > 0) {
            dataProvider = (String) ((Object[]) dataParameter)[0];
        }

        return dataProvider == null ? this.declaredMethod.getName() : this.declaredMethod.getName() + " [" + dataProvider + "]";
    }

    public String getTestMethodDescription() {
        return this.declaredMethod.getAnnotation(Test.class).description();
    }

    public String[] getTestGroups() {
        return this.invokedMethod.getTestMethod().getGroups();
    }

    /**
     * Get browser type base on the annotation/configs of each test case
     *
     * @return
     */
    public Browser getBrowserType() {
        if (this.browserType != null) {
            return this.browserType;
        }

        // Get browser type from retry method
        Browser retryBrowserType = null;
        IRetryAnalyzer analyzer = testResult.getMethod().getRetryAnalyzer();
        if (analyzer instanceof RetryAnalyzer) {
            retryBrowserType = ((RetryAnalyzer) analyzer).getRetryMethod(testResult).getBrowserType();
        }

        String browserTypeParam = this.invokedMethod.getTestMethod().getXmlTest().getParameter("browser");
        Browser configBrowserType = null;
        try {
            configBrowserType = Browser.valueOf(browserTypeParam.toUpperCase());
        } catch (Exception e) {
            throw new RuntimeException("Unsupported browser: " + configBrowserType);
        }

        // Override browser type
        FirefoxOnly firefoxOnly = this.declaredMethod.getAnnotation(FirefoxOnly.class);
        ChromeOnly chromeOnly = this.declaredMethod.getAnnotation(ChromeOnly.class);
        CaptureNetworkTraffic4Chrome captureNetworkTraffic4Chrome = this.declaredMethod.getAnnotation(CaptureNetworkTraffic4Chrome.class);

        if (retryBrowserType != null) {
            browserType = retryBrowserType;
        } else if (firefoxOnly != null) {
            browserType = Browser.FIREFOX;
        } else if (chromeOnly != null || captureNetworkTraffic4Chrome != null) {
            browserType = Browser.CHROME;
        } else if (configBrowserType == Browser.RANDOM) {
            browserType = Math.round(Math.random()) == 1 ? Browser.CHROME : Browser.FIREFOX;
        } else {
            // If no browser type matched, use RANDOM by default
            browserType = Browser.RANDOM;
        }

        return browserType;
    }

    /**
     * Get testing device dimension
     *
     * @return
     */
    public Dimension getDeviceDimension() {
        // Check the mobile screen size preference
        Device deviceAnnotationData = this.declaredMethod.getAnnotation(Device.class);
        Dimension deviceDimension;
        if (deviceAnnotationData != null) {
            int width = deviceAnnotationData.device() == DeviceName.OtherDevice ? deviceAnnotationData.width() : deviceAnnotationData.device().width;
            int height = deviceAnnotationData.device() == DeviceName.OtherDevice ? deviceAnnotationData.height() : deviceAnnotationData.device().height;
            deviceDimension = new Dimension(width, height);
        } else {
            // If device dimension is not specified, use desktop by default
            deviceDimension = new Dimension(DeviceName.DeskTopHD.width, DeviceName.DeskTopHD.height);
        }

        return deviceDimension;
    }

    public ChromeOptions getChromeOptions() {
        ChromeOptions options = new ChromeOptions();

        // Get Chrome options/arguments
        ChromeArguments chromeArguments = this.declaredMethod.getAnnotation(ChromeArguments.class);
        if (chromeArguments != null && chromeArguments.options().length > 0) {
            options.addArguments(chromeArguments.options());
        }

        // private mode
        IncognitoPrivateMode privateMode = this.declaredMethod.getAnnotation(IncognitoPrivateMode.class);
        if (privateMode != null) {
            options.addArguments("--incognito");
        }

        // headless mode
        HeadlessMode headlessMode = this.declaredMethod.getAnnotation(HeadlessMode.class);
        options.setHeadless(headlessMode != null);

        // Accept untrusted certificates
        AcceptUntrustedCertificates acceptUntrustedCertificates = this.declaredMethod.getAnnotation(AcceptUntrustedCertificates.class);
        options.setAcceptInsecureCerts(acceptUntrustedCertificates != null);

        CaptureNetworkTraffic4Chrome captureNetworkTraffic4Chrome = this.declaredMethod.getAnnotation(CaptureNetworkTraffic4Chrome.class);
        if (captureNetworkTraffic4Chrome != null) {
            LoggingPreferences preferences = new LoggingPreferences();
            preferences.enable(LogType.PERFORMANCE, Level.ALL);
            options.setCapability("goog:loggingPrefs", preferences);
        }

        return options;
    }

    public FirefoxOptions getFirefoxOptions() {
        FirefoxOptions options = new FirefoxOptions();
        // Get Firefox options/arguments
        FirefoxArguments firefoxArguments = this.declaredMethod.getAnnotation(FirefoxArguments.class);
        if (firefoxArguments != null && firefoxArguments.options().length > 0) {
            options.addArguments(firefoxArguments.options());
        }

        // private mode
        IncognitoPrivateMode privateMode = this.declaredMethod.getAnnotation(IncognitoPrivateMode.class);
        if (privateMode != null) {
            options.addArguments("-private");
        }

        // headless mode
        HeadlessMode headlessMode = this.declaredMethod.getAnnotation(HeadlessMode.class);
        options.setHeadless(headlessMode != null);

        // Accept untrusted certificates
        AcceptUntrustedCertificates acceptUntrustedCertificates = this.declaredMethod.getAnnotation(AcceptUntrustedCertificates.class);
        options.setAcceptInsecureCerts(acceptUntrustedCertificates != null);

        return options;
    }

    /**
     * Get browser options base on the annotation/configs of each test case
     *
     * @return
     */
    public MutableCapabilities getBrowserOption() {

        Browser browserType = this.getBrowserType();
        switch (browserType) {
            case CHROME: {
                return this.getChromeOptions();
            }
            case FIREFOX: {
                return this.getFirefoxOptions();
            }
            default:
                throw new RuntimeException("Unsupported browser: " + browserType);
        }
    }
}
