package com.scmp.framework.report;

import java.util.concurrent.ConcurrentHashMap;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtentTestManager {
  private static final Logger frameworkLogger = LoggerFactory.getLogger(ExtentTestManager.class);
  private static ExtentReports extent = ExtentManager.getExtent();
  private static ConcurrentHashMap<String, ExtentTest> extentReportMap = new ConcurrentHashMap<>();

  public static void removeTest(ExtentTest test) {
    extent.removeTest(test);
  }

  public static synchronized ExtentTest createTest(String name, String description) {
    ExtentTest test;
    String testNodeName = name;

    if (extentReportMap.containsKey(testNodeName)) {
      frameworkLogger.info("Reuse Test Thread ID: " + Thread.currentThread().getId() + ", Key: " + name);
      test = extentReportMap.get(testNodeName);
    } else {
      frameworkLogger.info(
          "Create new Test Thread ID: " + Thread.currentThread().getId() + ", Key: " + name);
      test = extent.createTest(name, description);
      extentReportMap.put(testNodeName, test);
    }

    return test;
  }
}
