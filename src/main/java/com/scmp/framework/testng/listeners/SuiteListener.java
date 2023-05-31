package com.scmp.framework.testng.listeners;

import com.scmp.framework.annotations.testrail.TestRailTestCase;
import com.scmp.framework.context.ApplicationContextProvider;
import com.scmp.framework.context.RunTimeContext;
import com.scmp.framework.testrail.TestRailManager;
import com.scmp.framework.testrail.TestRailStatus;
import com.scmp.framework.testrail.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestNGMethod;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.scmp.framework.utils.Constants.FILTERED_TEST_OBJECT;
import static com.scmp.framework.utils.Constants.TEST_RUN_OBJECT;

public class SuiteListener implements ISuiteListener {

	private static final Logger frameworkLogger = LoggerFactory.getLogger(SuiteListener.class);
	private final RunTimeContext runTimeContext;

	public SuiteListener() {
		ApplicationContext context = ApplicationContextProvider.getApplicationContext();
		runTimeContext = context.getBean(RunTimeContext.class);
	}

	@Override
	public void onFinish(ISuite suite) {
		frameworkLogger.info("Test Suite execution completed.");
	}

	@Override
	public void onStart(ISuite suite) {

		if (runTimeContext.getFrameworkConfigs().isTestRailUploadTestResult()) {
			try {
				initTestRail();
				createTestRun(suite);
			} catch (Exception e) {
				String errorMessage = "Fail to init and create Test Run in TestRail.";
				frameworkLogger.error(errorMessage, e);
				throw new RuntimeException(errorMessage);
			}
		}
	}

	private List<Integer> getAllTestRailTestCases(ISuite suite) {
		List<ITestNGMethod> testNGMethods = suite.getAllMethods();
		return testNGMethods.stream()
				.map(
						iTestNGMethod -> {
							TestRailTestCase testRailTestCase =
									iTestNGMethod
											.getConstructorOrMethod()
											.getMethod()
											.getAnnotation(TestRailTestCase.class);
							if (testRailTestCase!=null) {
								return testRailTestCase.id();
							} else {
								return null;
							}
						})
				.filter(out -> out!=null)
				.sorted()
				.collect(Collectors.toList());
	}

	private void initTestRail() {
		frameworkLogger.info("Initializing TestRailManager...");

		String baseUrl = runTimeContext.getFrameworkConfigs().getTestRailServer();
		String userName = runTimeContext.getFrameworkConfigs().getTestRailUserName();
		String password = runTimeContext.getFrameworkConfigs().getTestRailAPIKey();

		TestRailManager.init(baseUrl, userName, password);

		String inProgressId = runTimeContext.getFrameworkConfigs().getTestRailStatusInProgressId();
		if (inProgressId!=null && Pattern.compile("[0-9]+").matcher(inProgressId).matches()) {
			TestRailStatus.IN_PROGRESS = Integer.parseInt(inProgressId);
		} else {
			// Default use TestRailStatus.Retest for TestRailStatus.IN_PROGRESS
			TestRailStatus.IN_PROGRESS = TestRailStatus.Retest;
		}

		frameworkLogger.info("TestRailManager Initialized.");
	}

	private void createTestRun(ISuite suite) throws IOException {
		frameworkLogger.info("Creating Test Run in TestRail...");

		String projectId = runTimeContext.getFrameworkConfigs().getTestRailProjectId();

		if (projectId==null || !Pattern.compile("[0-9]+").matcher(projectId).matches()) {
			throw new IllegalArgumentException(
					String.format("Config TESTRAIL_PROJECT_ID [%s] is invalid!", projectId));
		}

		LocalDate today = LocalDate.now(runTimeContext.getZoneId());
		String timestamp = String.valueOf(today.minusDays(7).atStartOfDay(runTimeContext.getZoneId()).toEpochSecond());
		String todayDateString = today.format(DateTimeFormatter.ofPattern("M/dd/yyy"));
		String testRunName = runTimeContext.getFrameworkConfigs().getTestRailTestRunName();

		if (testRunName.isEmpty()) {
			// Default Test Run Name
			testRunName = String.format("Automated Test Run %s", today);
		} else {
			testRunName =
					testRunName
							.replace("${date}", todayDateString)
							.replace(
									"${FEATURE_DESCRIPTION}",
									runTimeContext.getFrameworkConfigs().getFeatureDescription());
		}

		final String finalTestRunName = testRunName.trim();

		if (!runTimeContext.getFrameworkConfigs().isTestRailCreateNewTestRun()) {
			TestRunResult testRunResult = TestRailManager.getInstance().getTestRuns(projectId, timestamp);
			Optional<TestRun> existingTestRun =
					testRunResult.getTestRunList().stream()
							.filter(testRun -> testRun.getName().trim().equalsIgnoreCase(finalTestRunName))
							.findFirst();

			if (existingTestRun.isPresent()) {
				// Use the existing TestRun for testing
				TestRun existingTestRunData = existingTestRun.get();
				frameworkLogger.info(
						"Use existing TestRun, Id: {}, Name: {}",
						existingTestRunData.getId(),
						existingTestRunData.getName());
				runTimeContext.setGlobalVariables(TEST_RUN_OBJECT, existingTestRunData);

				String statusFilter = runTimeContext.getFrameworkConfigs()
						.getTestRailTestStatusFilter().replace(" ", "");

				List<TestRunTest> matchedTests =
						TestRailManager.getInstance()
								.getAllTestRunTests(existingTestRunData.getId(), statusFilter);
				runTimeContext.setGlobalVariables(FILTERED_TEST_OBJECT, matchedTests);

				return;
			}
		}

		// Create a new test run
		// Look up test case ids
		List<Integer> testCaseIdList;
		if (runTimeContext.getFrameworkConfigs().isTestRailIncludeAllAutomatedTestCases()) {
			List<TestRunTest> testCaseList = TestRailManager.getInstance().getAllAutomatedTestCases(projectId);
			testCaseIdList = testCaseList.stream().map(TestRunTest::getId).collect(Collectors.toList());
		} else {
			testCaseIdList = this.getAllTestRailTestCases(suite);
		}

		if (testCaseIdList.size() > 0) {
			// Create test run
			TestRun testRun =
					TestRailManager.getInstance().addTestRun(projectId, finalTestRunName, testCaseIdList);
			// Save new created test run
			runTimeContext.setGlobalVariables(TEST_RUN_OBJECT, testRun);
			if (testRun!=null && testRun.getId() > 0) {
				frameworkLogger.info(
						"Test Run created in TestRail - Id: {}, Name: {}", testRun.getId(), testRun.getName());
			} else {
				frameworkLogger.error("Failed to create Test Run in TestRail.");
				throw new RuntimeException("Failed to create Test Run in TestRail.");
			}
		} else {
			frameworkLogger.warn(
					"Test Run is NOT created in TestRail, empty Test Case List is detected.");
		}
	}
}
