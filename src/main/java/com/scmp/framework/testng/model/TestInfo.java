package com.scmp.framework.testng.model;

import com.scmp.framework.annotations.*;
import com.scmp.framework.annotations.screens.Device;
import com.scmp.framework.annotations.screens.DeviceName;
import com.scmp.framework.annotations.testrail.TestRailTestCase;
import com.scmp.framework.context.RunTimeContext;
import com.scmp.framework.model.Browser;
import com.scmp.framework.testng.listeners.RetryAnalyzer;
import com.scmp.framework.testrail.TestRailDataHandler;
import com.scmp.framework.testrail.TestRailStatus;
import com.scmp.framework.testrail.models.TestRun;
import com.scmp.framework.testrail.models.TestRunTest;
import com.scmp.framework.utils.ConfigFileReader;
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
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

import static com.scmp.framework.utils.ConfigFileKeys.LOCAL_STORAGE_DATA_PATH;
import static com.scmp.framework.utils.ConfigFileKeys.PRELOAD_LOCAL_STORAGE_DATA;
import static com.scmp.framework.utils.Constants.FILTERED_TEST_OBJECT;
import static com.scmp.framework.utils.Constants.TEST_RUN_OBJECT;

public class TestInfo {
  private IInvokedMethod testNGInvokedMethod;
  private ITestResult testResult;
  private Method declaredMethod;

  private Browser browserType = null;
  private TestRailDataHandler testRailDataHandler = null;
  private LocalDateTime testStartTime = null;
  private LocalDateTime testEndTime = null;
  private Boolean isSkippedTest = null;

  public TestInfo(IInvokedMethod methodName, ITestResult testResult) {
    this.testNGInvokedMethod = methodName;
    this.testResult = testResult;
    this.declaredMethod =
        this.testNGInvokedMethod.getTestMethod().getConstructorOrMethod().getMethod();
    this.testStartTime = LocalDateTime.now(RunTimeContext.getInstance().getZoneId());

    // Init TestRail handler
    if (this.isTestMethod()
        && !this.isSkippedTest()
        && RunTimeContext.getInstance().isUploadToTestRail()) {
      TestRailTestCase testRailCase = this.declaredMethod.getAnnotation(TestRailTestCase.class);
      TestRun testRun = (TestRun) RunTimeContext.getInstance().getGlobalVariables(TEST_RUN_OBJECT);
      if (testRailCase != null && testRun != null) {
        this.testRailDataHandler = new TestRailDataHandler(testRailCase.id(), testRun);
      }
    }
  }

  public void addTestResultForTestRail(int status, String content, String filePath) {
    if (this.testRailDataHandler != null) {
      this.testRailDataHandler.addStepResult(status, content, filePath);
    }
  }

  public void setTestEndTime() {
    this.testEndTime = LocalDateTime.now(RunTimeContext.getInstance().getZoneId());
  }

  public void uploadTestResultsToTestRail() {
    if (this.testRailDataHandler != null) {
      int finalTestResult = TestRailStatus.Untested;
      switch (this.testResult.getStatus()) {
        case ITestResult.SUCCESS:
          finalTestResult = TestRailStatus.Passed;
          break;
        case ITestResult.FAILURE:
          finalTestResult = TestRailStatus.Failed;
          break;
        default:
          finalTestResult = TestRailStatus.Untested;
      }

      if (this.testEndTime == null) {
        this.setTestEndTime();
      }

      long elapsed = Duration.between(this.testStartTime, this.testEndTime).getSeconds();
      this.testRailDataHandler.uploadDataToTestRail(finalTestResult, elapsed);
    }
  }

  public ITestResult getTestResult() {
    return this.testResult;
  }

  public IInvokedMethod getTestNGInvokedMethod() {
    return this.testNGInvokedMethod;
  }

  public Method getDeclaredMethod() {
    return this.declaredMethod;
  }

  public String getClassName() {
    return this.declaredMethod.getDeclaringClass().getSimpleName();
  }

  public String[] getClassLevelGroups() {
    Test testNgTest = this.declaredMethod.getDeclaringClass().getAnnotation(Test.class);
    return testNgTest == null ? null : testNgTest.groups();
  }

  public String getClassDescription() {
    Test description = this.declaredMethod.getDeclaringClass().getAnnotation(Test.class);
    return description == null ? "" : description.description();
  }

  public String getMethodName() {
    return this.declaredMethod.getName();
  }

  public boolean isTestMethod() {
    return this.declaredMethod.getAnnotation(Test.class) != null;
  }

  public String[] getAuthorNames() {
    return declaredMethod.getAnnotation(Authors.class) == null
        ? null
        : declaredMethod.getAnnotation(Authors.class).name();
  }

  public String getTestName() {
    String dataProvider = null;
    Object dataParameter = this.testNGInvokedMethod.getTestResult().getParameters();
    if (((Object[]) dataParameter).length > 0) {
      dataProvider = (String) ((Object[]) dataParameter)[0];
    }

    return dataProvider == null
        ? this.declaredMethod.getName()
        : this.declaredMethod.getName() + " [" + dataProvider + "]";
  }

  public String getTestMethodDescription() {
    return this.declaredMethod.getAnnotation(Test.class).description();
  }

  public String[] getTestGroups() {
    return this.testNGInvokedMethod.getTestMethod().getGroups();
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

    String browserTypeParam =
        this.testNGInvokedMethod.getTestMethod().getXmlTest().getParameter("browser");
    Browser configBrowserType = null;
    try {
      configBrowserType = Browser.valueOf(browserTypeParam.toUpperCase());
    } catch (Exception e) {
      throw new RuntimeException("Unsupported browser: " + configBrowserType);
    }

    // Override browser type
    FirefoxOnly firefoxOnly = this.declaredMethod.getAnnotation(FirefoxOnly.class);
    ChromeOnly chromeOnly = this.declaredMethod.getAnnotation(ChromeOnly.class);
    CaptureNetworkTraffic4Chrome captureNetworkTraffic4Chrome =
        this.declaredMethod.getAnnotation(CaptureNetworkTraffic4Chrome.class);

    // Further update browser type base on annotation
    if (retryBrowserType != null) {
      browserType = retryBrowserType;
    } else if (firefoxOnly != null) {
      browserType = Browser.FIREFOX;
    } else if (chromeOnly != null || captureNetworkTraffic4Chrome != null) {
      browserType = Browser.CHROME;
    } else if (configBrowserType == Browser.RANDOM) {
      browserType = Math.round(Math.random()) == 1 ? Browser.CHROME : Browser.FIREFOX;
    } else {
      // Default browser type value from config
      browserType = configBrowserType;
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
      int width =
          deviceAnnotationData.device() == DeviceName.OtherDevice
              ? deviceAnnotationData.width()
              : deviceAnnotationData.device().width;
      int height =
          deviceAnnotationData.device() == DeviceName.OtherDevice
              ? deviceAnnotationData.height()
              : deviceAnnotationData.device().height;
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
    IncognitoPrivateMode privateMode =
        this.declaredMethod.getAnnotation(IncognitoPrivateMode.class);
    if (privateMode != null) {
      options.addArguments("--incognito");
    }

    // headless mode
    HeadlessMode headlessMode = this.declaredMethod.getAnnotation(HeadlessMode.class);
    options.setHeadless(headlessMode != null);

    // Accept untrusted certificates
    AcceptUntrustedCertificates acceptUntrustedCertificates =
        this.declaredMethod.getAnnotation(AcceptUntrustedCertificates.class);
    options.setAcceptInsecureCerts(acceptUntrustedCertificates != null);

    CaptureNetworkTraffic4Chrome captureNetworkTraffic4Chrome =
        this.declaredMethod.getAnnotation(CaptureNetworkTraffic4Chrome.class);
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
    IncognitoPrivateMode privateMode =
        this.declaredMethod.getAnnotation(IncognitoPrivateMode.class);
    if (privateMode != null) {
      options.addArguments("-private");
    }

    // headless mode
    HeadlessMode headlessMode = this.declaredMethod.getAnnotation(HeadlessMode.class);
    options.setHeadless(headlessMode != null);

    // Accept untrusted certificates
    AcceptUntrustedCertificates acceptUntrustedCertificates =
        this.declaredMethod.getAnnotation(AcceptUntrustedCertificates.class);
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
      case CHROME:
        {
          return this.getChromeOptions();
        }
      case FIREFOX:
        {
          return this.getFirefoxOptions();
        }
      default:
        throw new RuntimeException("Unsupported browser: " + browserType);
    }
  }

  /**
   * Get the local storage data from 1. config.properties: LOCAL_STORAGE_DATA_PATH 2.
   * CustomLocalStorage: path 3. CustomLocalStorage: LocalStorageData
   *
   * @return
   */
  public Map<String, String> getCustomLocalStorage() {

    Map<String, String> customData = new HashMap<>();
    boolean loadDefaultData =
        "true"
            .equalsIgnoreCase(
                RunTimeContext.getInstance().getProperty(PRELOAD_LOCAL_STORAGE_DATA, "false"));
    CustomLocalStorage customLocalStorage =
        this.declaredMethod.getAnnotation(CustomLocalStorage.class);
    loadDefaultData =
        customLocalStorage != null && customLocalStorage.loadDefault()
            ? customLocalStorage.loadDefault()
            : loadDefaultData;

    // Load default data
    if (loadDefaultData) {
      String filePath = RunTimeContext.getInstance().getProperty(LOCAL_STORAGE_DATA_PATH);
      customData.putAll(new ConfigFileReader(filePath).getAllProperties());
    }

    // Load custom data file
    if (customLocalStorage != null && !"".equalsIgnoreCase(customLocalStorage.path().trim())) {
      String filePath = customLocalStorage.path().trim();
      customData.putAll(new ConfigFileReader(filePath).getAllProperties());
    }

    // Load custom data
    if (customLocalStorage != null && customLocalStorage.data().length > 0) {
      for (LocalStorageData data : customLocalStorage.data()) {
        customData.put(data.key(), data.value());
      }
    }

    return customData;
  }

  public boolean isInTestRailTestList() {

    Object filteredTestsObject =
        RunTimeContext.getInstance().getGlobalVariables(FILTERED_TEST_OBJECT);
    if (filteredTestsObject != null && filteredTestsObject instanceof List) {
      List<TestRunTest> filteredTests = (List<TestRunTest>) filteredTestsObject;

      TestRailTestCase testRailTestCase = this.declaredMethod.getAnnotation(TestRailTestCase.class);
      Optional<TestRunTest> result =
          filteredTests.parallelStream()
              .filter(test -> test.getCaseId() == testRailTestCase.id())
              .findFirst();

      if (result.isPresent()) {
        return true;
      } else {
        return false;
      }
    }

    return true;
  }

  public boolean isSkippedTest() {
    if (this.isSkippedTest == null) {
      this.isSkippedTest = !this.isInTestRailTestList();
    }

    return this.isSkippedTest;
  }

  public boolean needLaunchBrowser() {
    LaunchBrowser launchBrowser = this.declaredMethod.getAnnotation(LaunchBrowser.class);
    if (launchBrowser != null && !launchBrowser.status()) {
      return false;
    } else {
      return true;
    }
  }
}
