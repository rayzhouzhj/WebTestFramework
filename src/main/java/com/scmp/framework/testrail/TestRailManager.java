package com.scmp.framework.testrail;

import com.scmp.framework.testrail.models.*;
import com.scmp.framework.testrail.models.requests.AddTestResultRequest;
import com.scmp.framework.testrail.models.requests.AddTestRunRequest;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class TestRailManager {
  private static final Logger frameworkLogger = LoggerFactory.getLogger(TestRailManager.class);
  private static TestRailManager instance;
  private Retrofit retrofit;

  public static synchronized void init(String baseUrl, String userName, String apiKey) {

    if (baseUrl == null
        || baseUrl.isEmpty()
        || userName == null
        || userName.isEmpty()
        || apiKey == null
        || apiKey.isEmpty()) {
      throw new IllegalArgumentException(
          String.format(
              "IllegalArgument found: BaseUrl=[%s], UserName=[%s], APIKey=[%s]",
              baseUrl, userName, apiKey));
    }

    instance = new TestRailManager(baseUrl, userName, apiKey);
  }

  public static TestRailManager getInstance() {
    return instance;
  }

  private TestRailManager(String baseUrl, String userName, String apiKey) {

    if (!baseUrl.endsWith("/")) {
      baseUrl += "/";
    }

    OkHttpClient client =
        new OkHttpClient.Builder()
            .addInterceptor(new BasicAuthInterceptor(userName, apiKey))
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    retrofit =
        new Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
  }

  public TestRunResult getTestRuns(String projectId, String timestamp) throws IOException {
    String CustomQuery = String.format(TestRailService.GET_TEST_RUNS_API, projectId);
    TestRailService service = retrofit.create(TestRailService.class);

    Map<String, String> data = new LinkedHashMap<>();
    data.put(CustomQuery, "");
    data.put("created_after", timestamp);

    retrofit2.Response<TestRunResult> response = service.getTestRuns(data).execute();
    if (!response.isSuccessful()) {
      frameworkLogger.error(
          "Request getTestRuns Failed with Error Code: {}, Error Body: {}",
          response.code(),
          response.errorBody().string());
      frameworkLogger.error("ProjectId: {}, Timestamp: {}", projectId, timestamp);
    }

    return response.body();
  }

  public TestCaseResult getAutomatedTestCases(String projectId, Integer offset) throws IOException {
    String CustomQuery = String.format(TestRailService.GET_TEST_CASES_API, projectId);
    TestRailService service = retrofit.create(TestRailService.class);

    Map<String, String> data = new LinkedHashMap<>();
    data.put(CustomQuery, "");
    data.put(TestCase.TYPE_ID, TestCase.TYPE_AUTOMATED);
    data.put("offset", offset.toString());

    retrofit2.Response<TestCaseResult> response = service.getTestCases(data).execute();
    if (!response.isSuccessful()) {
      frameworkLogger.error(
          "Request getTestCases Failed with Error Code: {}, Error Body: {}",
          response.code(),
          response.errorBody().string());
      frameworkLogger.error("ProjectId: {}", projectId);
    }

    return response.body();
  }

  public List<TestRunTest> getAllAutomatedTestCases(String projectId) throws IOException {

    List<TestRunTest> allResults = new ArrayList<>();
    int offset = 0;
    boolean withNextPage = true;
    while (withNextPage) {
      TestCaseResult result = this.getAutomatedTestCases(projectId, offset);
      if (result.getTestRunTestList().size() > 0) {
        allResults.addAll(result.getTestRunTestList());
      }

      if(result.getPagingLinks().getNext() != null) {
        offset += result.getSize();
      } else {
        withNextPage = false;
      }
    }

    return allResults;
  }

  public List<TestResult> getTestResultsForTestCase(int runId, int testcaseId) throws IOException {
    String CustomQuery =
        String.format(TestRailService.GET_TEST_RESULTS_FOR_TEST_CASE_API, runId, testcaseId);
    TestRailService service = retrofit.create(TestRailService.class);

    Map<String, String> data = new LinkedHashMap<>();
    data.put(CustomQuery, "");

    retrofit2.Response<List<TestResult>> response =
        service.getTestResultsForTestCase(data).execute();
    if (!response.isSuccessful()) {
      frameworkLogger.error(
          "Request getTestCases Failed with Error Code: {}, Error Body: {}",
          response.code(),
          response.errorBody().string());
      frameworkLogger.error("RunId: {}, TestCaseId: {}", runId, testcaseId);
    }

    return response.body();
  }

  public TestRun getTestRun(String testRunId) throws IOException {
    String CustomQuery = String.format(TestRailService.GET_TEST_RUN_API, testRunId);
    TestRailService service = retrofit.create(TestRailService.class);

    Map<String, String> data = new LinkedHashMap<>();
    data.put(CustomQuery, "");

    retrofit2.Response<TestRun> response = service.getTestRun(data).execute();
    if (!response.isSuccessful()) {
      frameworkLogger.error(
          "Request getTestRun Failed with Error Code: {}, Error Body: {}",
          response.code(),
          response.errorBody().string());
      frameworkLogger.error("RunId: {}", testRunId);
    }

    return response.body();
  }

  public TestRunTestResult getTestRunTests(int testRunId, String statusFilterString) throws IOException {
    String CustomQuery = String.format(TestRailService.GET_TESTS_API, testRunId);
    TestRailService service = retrofit.create(TestRailService.class);

    Map<String, String> data = new LinkedHashMap<>();
    data.put(CustomQuery, "");
    data.put("status_id", statusFilterString);

    retrofit2.Response<TestRunTestResult> response = service.getTestRunTests(data).execute();
    if (!response.isSuccessful()) {
      frameworkLogger.error(
              "Request getTestRunTests Failed with Error Code: {}, Error Body: {}",
              response.code(),
              response.errorBody().string());
      frameworkLogger.error("RunId: {}, Status: {}", testRunId, statusFilterString);
    }

    return response.body();
  }

  public TestRun addTestRun(String projectId, String testRunName, List<Integer> includeTestCaseIds)
      throws IOException {
    if (includeTestCaseIds == null) {
      includeTestCaseIds = new ArrayList<>();
    }

    AddTestRunRequest request =
        new AddTestRunRequest(testRunName, includeTestCaseIds.size() == 0, includeTestCaseIds);

    String CustomQuery = String.format(TestRailService.ADD_TEST_RUN_API, projectId);
    TestRailService service = retrofit.create(TestRailService.class);

    Map<String, String> data = new LinkedHashMap<>();
    data.put(CustomQuery, "");
    TestRun testRun = null;

    retrofit2.Response<TestRun> response = service.addTestRun(data, request).execute();
    if (!response.isSuccessful()) {
      frameworkLogger.error(
          "Request addTestRun Failed with Error Code: {}, Error Body: {}",
          response.code(),
          response.errorBody().string());
      frameworkLogger.error(
          "ProjectId: {}, TestRunName: {}, includeTestCaseIds: {}",
          projectId,
          testRunName,
          includeTestCaseIds);
    } else {
      testRun = response.body();
      testRun.setTestCaseIds(includeTestCaseIds);
    }

    return testRun;
  }

  public TestRun updateTestRun(TestRun testRun) throws IOException {

    AddTestRunRequest request =
        new AddTestRunRequest(testRun.getName(), testRun.getIncludeAll(), testRun.getTestCaseIds());

    String CustomQuery = String.format(TestRailService.UPDATE_TEST_RUN_API, testRun.getId());
    TestRailService service = retrofit.create(TestRailService.class);

    Map<String, String> data = new LinkedHashMap<>();
    data.put(CustomQuery, "");

    TestRun updatedTestRun = null;
    retrofit2.Response<TestRun> response = service.updateTestRun(data, request).execute();
    if (!response.isSuccessful()) {
      frameworkLogger.error(
          "Request updateTestRun Failed with Error Code: {}, Error Body: {}",
          response.code(),
          response.errorBody().string());
      frameworkLogger.error("RunName: {}", request.getName());
    } else {
      updatedTestRun = response.body();
      updatedTestRun.setTestCaseIds(testRun.getTestCaseIds());
    }

    return updatedTestRun;
  }

  public TestResult addTestResult(
      Integer testRunId, Integer testCaseId, AddTestResultRequest request) throws IOException {

    String CustomQuery =
        String.format(TestRailService.ADD_RESULT_FOR_TEST_CASE_API, testRunId, testCaseId);
    TestRailService service = retrofit.create(TestRailService.class);

    Map<String, String> data = new LinkedHashMap<>();
    data.put(CustomQuery, "");

    retrofit2.Response<TestResult> response = service.addResultForTestCase(data, request).execute();
    if (!response.isSuccessful()) {
      frameworkLogger.error(
          "Request addResultForTestCase Failed with Error Code: {}, Error Body: {}",
          response.code(),
          response.errorBody().string());
      frameworkLogger.error("RunId: {}, TestCaseId: {}", testRunId, testCaseId);
    }

    return response.body();
  }

  public Attachment addAttachmentToTestRun(Integer testRunId, String imagePath) throws IOException {

    String CustomQuery = String.format(TestRailService.ADD_ATTACHMENT_FOR_TEST_RUN_API, testRunId);
    TestRailService service = retrofit.create(TestRailService.class);

    Map<String, String> data = new LinkedHashMap<>();
    data.put(CustomQuery, "");

    File file = new File(imagePath);
    RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
    MultipartBody.Part imageToUpload =
        MultipartBody.Part.createFormData("attachment", file.getName(), requestFile);

    retrofit2.Response<Attachment> response = service.addAttachment(data, imageToUpload).execute();
    if (!response.isSuccessful()) {
      frameworkLogger.error(
          "Request addAttachment Failed with Error Code: {}, Error Body: {}",
          response.code(),
          response.errorBody().string());
      frameworkLogger.error("RunId: {}", testRunId);
    }

    return response.body();
  }

  public Attachment addAttachmentToTestResult(Integer testResultId, String imagePath)
      throws IOException {

    String CustomQuery =
        String.format(TestRailService.ADD_ATTACHMENT_FOR_TEST_RESULT_API, testResultId);
    TestRailService service = retrofit.create(TestRailService.class);

    Map<String, String> data = new LinkedHashMap<>();
    data.put(CustomQuery, "");

    File file = new File(imagePath);
    RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
    MultipartBody.Part imageToUpload =
        MultipartBody.Part.createFormData("attachment", file.getName(), requestFile);

    retrofit2.Response<Attachment> response = service.addAttachment(data, imageToUpload).execute();
    if (!response.isSuccessful()) {
      frameworkLogger.error(
          "Request addAttachment Failed with Error Code: {}, Error Body: {}",
          response.code(),
          response.errorBody().string());
      frameworkLogger.error("TestResultId: {}, Image: {}", testResultId, imagePath);
    }

    return response.body();
  }
}

class BasicAuthInterceptor implements Interceptor {

  private String credentials;

  public BasicAuthInterceptor(String user, String password) {
    this.credentials = Credentials.basic(user, password);
  }

  @Override
  public Response intercept(Chain chain) throws IOException {
    Request request = chain.request();
    Request authenticatedRequest =
        request.newBuilder().header("Authorization", credentials).build();
    return chain.proceed(authenticatedRequest);
  }
}
