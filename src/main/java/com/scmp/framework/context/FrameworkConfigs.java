package com.scmp.framework.context;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("file:config.properties")
@Getter
public class FrameworkConfigs {
	
	@Value("${BROWSER_TYPE}")
	private String browserType;

	@Value("${HOST_URL}")
	private String hostUrl;

	@Value("${GLOBAL_CHROME_OPTIONS}")
	private String globalChromeOptions;

	@Value("${PRELOAD_LOCAL_STORAGE_DATA}")
	private boolean preloadLocalStorageData;

	@Value("${LOCAL_STORAGE_DATA_PATH}")
	private String localStorageDataPath;

	@Value("${DEFAULT_LOCAL_EXTENSION_PATH}")
	private String defaultLocalExtensionPath;

	@Value("${DEFAULT_REMOTE_EXTENSION_PATH}")
	private String defaultRemoteExtensionPath;

	@Value("${FRAMEWORK}")
	private String framework;

	@Value("${THREAD_COUNT}")
	private int threadCount;

	@Value("${DATAPROVIDER_THREAD_COUNT}")
	private int dataProviderThreadCount;

	@Value("${MAX_RETRY_COUNT}")
	private int maxRetryCount;

	@Value("${REMOVE_FAILED_TEST_B4_RETRY:false}")
	private boolean removeFailedTestB4Retry;

	@Value("${TESTRAIL_SERVER}")
	private String testRailServer;

	@Value("${TESTRAIL_USER_NAME}")
	private String testRailUserName;

	@Value("${TESTRAIL_API_KEY}")
	private String testRailAPIKey;

	@Value("${TESTRAIL_TEST_RUN_NAME}")
	private String testRailTestRunName;

	@Value("${TESTRAIL_PROJECT_ID}")
	private String testRailProjectId;

	@Value("${TESTRAIL_STATUS_IN_PROGRESS_ID}")
	private String testRailStatusInProgressId;

	@Value("${TESTRAIL_CREATE_NEW_TEST_RUN}")
	private boolean testRailCreateNewTestRun;

	@Value("${TESTRAIL_TEST_STATUS_FILTER}")
	private String testRailTestStatusFilter;

	@Value("${TESTRAIL_INCLUDE_ALL_AUTOMATED_TEST_CASES}")
	private boolean testRailIncludeAllAutomatedTestCases;

	@Value("${TESTRAIL_UPLOAD_FLAG}")
	private boolean testRailUploadTestResult;

	@Value("${EXCLUDE_GROUPS}")
	private String excludeGroups;

	@Value("${INCLUDE_GROUPS}")
	private String includeGroups;

	@Value("${URL}")
	private String url;

	@Value("${FEATURE_DESCRIPTION}")
	private String featureDescription;

	@Value("${DRIVER_HOME}")
	private String driverHome;

	@Value("${LOCAL_EXECUTION:OFF}")
	private String localExecutionMode;

	@Value("${EXTENT_XML_PATH}")
	private String extentXMLPath;
}
