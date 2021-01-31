package com.scmp.framework.report;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.configuration.ChartLocation;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.scmp.framework.context.RunTimeContext;
import com.scmp.framework.utils.ConfigFileKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.scmp.framework.utils.Constants.TARGET_PATH;

public class ExtentManager {
  private static final Logger frameworkLogger = LoggerFactory.getLogger(ExtentManager.class);

  private static ExtentReports extent;
  private static final String filePath = TARGET_PATH + File.separator + "WebTestReport.html";
  private static final String extentXML = "extent.xml";

  public static synchronized ExtentReports getExtent() {
    if (extent == null) {
      extent = new ExtentReports();
      extent.attachReporter(getHtmlReporter());

      String browserType = RunTimeContext.getInstance().getProperty(ConfigFileKeys.BROWSER_TYPE);
      String threadCount = RunTimeContext.getInstance().getProperty(ConfigFileKeys.THREAD_COUNT);
      String excludeGroups =
          RunTimeContext.getInstance().getProperty(ConfigFileKeys.EXCLUDE_GROUPS);
      String includeGroups =
          RunTimeContext.getInstance().getProperty(ConfigFileKeys.INCLUDE_GROUPS);
      String url = RunTimeContext.getInstance().getProperty(ConfigFileKeys.URL);
      String featureDesc =
          RunTimeContext.getInstance().getProperty(ConfigFileKeys.FEATURE_DESCRIPTION);

      extent.setSystemInfo("URL", url);
      extent.setSystemInfo("Include Groups", includeGroups);
      extent.setSystemInfo("Exclude Groups", excludeGroups);
      extent.setSystemInfo("Browser Type", browserType);
      extent.setSystemInfo("Tread Count", threadCount);
      extent.setSystemInfo("Feature", featureDesc);

      List<Status> statusHierarchy =
          Arrays.asList(
              Status.FATAL,
              Status.FAIL,
              Status.ERROR,
              Status.WARNING,
              Status.PASS,
              Status.SKIP,
              Status.DEBUG,
              Status.INFO);

      extent.config().statusConfigurator().setStatusHierarchy(statusHierarchy);
    }

    return extent;
  }

  private static ExtentHtmlReporter getHtmlReporter() {
    ExtentHtmlReporter htmlReporter = new ExtentHtmlReporter(filePath);
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    URL resource = classLoader.getResource(extentXML);
    if (resource != null) {
      frameworkLogger.info("Loading extent.xml from {}", resource.getPath());
      htmlReporter.loadXMLConfig(resource.getPath());
    } else {
      frameworkLogger.warn("Cannot load extent.xml, using default value");
    }

    // make the charts visible on report open
    htmlReporter.config().setChartVisibilityOnOpen(true);

    // report title
    htmlReporter.config().setDocumentTitle("WEB Test Report");
    htmlReporter.config().setReportName("WEB Test Report");
    htmlReporter.config().setTestViewChartLocation(ChartLocation.TOP);
    htmlReporter.config().setTheme(Theme.STANDARD);

    return htmlReporter;
  }

  public static void flush() {
    extent.flush();
  }
}
