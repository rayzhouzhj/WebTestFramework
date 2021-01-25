package com.scmp.framework.testrail;

import com.scmp.framework.testrail.models.TestCase;
import com.scmp.framework.testrail.models.TestRun;
import com.scmp.framework.testrail.models.requests.AddTestResultRequest;
import com.scmp.framework.testrail.models.requests.AddTestRunRequest;
import okhttp3.*;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestRailManager {
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
            .build();

    retrofit =
        new Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
  }

  public List<TestRun> getTestRuns(String projectID) {
    String CustomQuery = String.format(TestRailService.GET_TEST_RUNS_API, projectID);

    TestRailService service = retrofit.create(TestRailService.class);
    List<TestRun> testRunList = null;
    try {
      Map<String, String> data = new HashMap<>();
      data.put(CustomQuery, "");
      testRunList = service.getTestRuns(data).execute().body();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return testRunList;
  }

  public List<TestCase> getAutomatedTestCases(String projectId) {
    String CustomQuery = String.format(TestRailService.GET_TEST_CASES_API, projectId);

    TestRailService service = retrofit.create(TestRailService.class);
    List<TestCase> testCaseList = null;
    try {
      Map<String, String> data = new HashMap<>();
      data.put(CustomQuery, "");
      data.put(TestCase.TYPE_ID, TestCase.TYPE_AUTOMATED);
      testCaseList = service.getTestCases(data).execute().body();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return testCaseList;
  }

  public TestRun getTestRun(String testRunId) {
    String CustomQuery = String.format(TestRailService.GET_TEST_RUN_API, testRunId);

    TestRailService service = retrofit.create(TestRailService.class);
    TestRun testRun = null;
    try {
      Map<String, String> data = new HashMap<>();
      data.put(CustomQuery, "");
      testRun = service.getTestRun(data).execute().body();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return testRun;
  }

  public TestRun addTestRun(String projectId, String testRunName, List<Integer> includeTestCaseIds) {
    if(includeTestCaseIds == null) {
      includeTestCaseIds = new ArrayList<>();
    }

    AddTestRunRequest request = new AddTestRunRequest(
            testRunName,
            includeTestCaseIds.size() == 0,
            includeTestCaseIds);

    String CustomQuery = String.format(TestRailService.ADD_TEST_RUN_API, projectId);

    TestRailService service = retrofit.create(TestRailService.class);
    TestRun testRun = null;
    try {
      Map<String, String> data = new HashMap<>();
      data.put(CustomQuery, "");
      testRun = service.addTestRun(data, request).execute().body();
    } catch (IOException e) {
      e.printStackTrace();
    }

    testRun.setTestCaseIds(includeTestCaseIds);

    return testRun;
  }

  public TestRun updateTestRun(TestRun testRun) {

    AddTestRunRequest request = new AddTestRunRequest(
            testRun.getName(),
            testRun.getIncludeAll(),
            testRun.getTestCaseIds());

    String CustomQuery = String.format(TestRailService.UPDATE_TEST_RUN_API, testRun.getId());

    TestRailService service = retrofit.create(TestRailService.class);
    TestRun updatedTestRun = null;
    try {
      Map<String, String> data = new HashMap<>();
      data.put(CustomQuery, "");
      updatedTestRun = service.updateTestRun(data, request).execute().body();
      updatedTestRun.setTestCaseIds(testRun.getTestCaseIds());
    } catch (IOException e) {
      e.printStackTrace();
    }

    return updatedTestRun;
  }

  public void addTestResult(Integer testRunId, Integer testCaseId, AddTestResultRequest request) {

    String CustomQuery = String.format(TestRailService.ADD_RESULT_FOR_TEST_CASE_API, testRunId, testCaseId);

    TestRailService service = retrofit.create(TestRailService.class);
    try {
      Map<String, String> data = new HashMap<>();
      data.put(CustomQuery, "");
      service.addResultForTestCase(data, request).execute().body();
    } catch (IOException e) {
      e.printStackTrace();
    }
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
