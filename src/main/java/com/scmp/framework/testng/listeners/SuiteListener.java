package com.scmp.framework.testng.listeners;

import com.scmp.framework.context.RunTimeContext;
import com.scmp.framework.testrail.TestRailManager;
import com.scmp.framework.testrail.models.TestCase;
import com.scmp.framework.testrail.models.TestRun;
import com.scmp.framework.utils.ConfigFileKeys;
import org.testng.ISuite;
import org.testng.ISuiteListener;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.scmp.framework.utils.Constants.TEST_RUN_OBJECT;

public class SuiteListener implements ISuiteListener {
  @Override
  public void onFinish(ISuite suite) {
    System.out.println("onFinish function started of ISuiteListener ");
  }

  @Override
  public void onStart(ISuite suite) {

    if (RunTimeContext.getInstance().isUploadToTestRail()) {
      try {
        initTestRail();
        createTestRun();
      } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException("Fail to init and create Test Run in TestRail.");
      }
    }
  }

  public void initTestRail() {
    RunTimeContext instance = RunTimeContext.getInstance();
    String baseUrl = instance.getProperty(ConfigFileKeys.TESTRAIL_SERVER);
    String userName = instance.getProperty(ConfigFileKeys.TESTRAIL_USER_NAME);
    String password = instance.getProperty(ConfigFileKeys.TESTRAIL_API_KEY);

    TestRailManager.init(baseUrl, userName, password);
  }

  public void createTestRun() throws IOException {

    RunTimeContext instance = RunTimeContext.getInstance();
    String projectId = instance.getProperty(ConfigFileKeys.TESTRAIL_PROJECT_ID);

    if (projectId == null || Pattern.compile("[0-9]+").matcher(projectId).matches()) {
      throw new IllegalArgumentException(String.format("Config TESTRAIL_PROJECT_ID [%s] is invalid!", projectId));
    }

    LocalDate today = LocalDate.now(instance.getZoneId());
    String timestamp = "" + today.minusDays(1).atStartOfDay(instance.getZoneId()).toEpochSecond();
    String todayDateString = LocalDate.now(instance.getZoneId()).format(DateTimeFormatter.ofPattern("dd/MM/yyy"));
    String testRunName = instance.getProperty(ConfigFileKeys.TESTRAIL_TEST_RUN_NAME, "");

    if (testRunName.isEmpty()) {
      // Default Test Run Name
      testRunName = String.format("Automated Test Run %s", today);
    } else {
      testRunName =
          testRunName
              .replace("${date}", todayDateString)
              .replace(
                  "${FEATURE_DESCRIPTION}",
                  instance.getProperty(ConfigFileKeys.FEATURE_DESCRIPTION, ""));
    }

    final String finalTestRunName = testRunName;
    if (!instance.isCreateNewTestRunInTestRail()) {
      List<TestRun> testRunList = TestRailManager.getInstance().getTestRuns(projectId, timestamp);
      TestRun existingTestRun =
          testRunList.stream()
              .filter(testRun -> testRun.getName().equalsIgnoreCase(finalTestRunName))
              .findFirst()
              .get();

      if (existingTestRun != null) {
        // Use the existing TestRun for testing
        instance.setGlobalVariables(TEST_RUN_OBJECT, existingTestRun);
        return;
      }
    }

    // Create new test run
    List<TestCase> testCaseList = TestRailManager.getInstance().getAutomatedTestCases(projectId);
    List<Integer> testCaseIdList =
        testCaseList.stream().map(testCase -> testCase.getId()).collect(Collectors.toList());
    TestRun testRun =
        TestRailManager.getInstance().addTestRun(projectId, finalTestRunName, testCaseIdList);

    // Save new created test run
    instance.setGlobalVariables(TEST_RUN_OBJECT, testRun);
  }
}
