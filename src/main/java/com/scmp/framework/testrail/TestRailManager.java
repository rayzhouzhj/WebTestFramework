package com.scmp.framework.testrail;

import com.scmp.framework.testrail.models.Attachment;
import com.scmp.framework.testrail.models.TestCase;
import com.scmp.framework.testrail.models.TestRun;
import com.scmp.framework.testrail.models.requests.AddTestResultRequest;
import com.scmp.framework.testrail.models.requests.AddTestRunRequest;
import okhttp3.*;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.File;
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

  public List<TestRun> getTestRuns(String projectId, String timestamp) throws IOException {
    String CustomQuery = String.format(TestRailService.GET_TEST_RUNS_API, projectId);
    TestRailService service = retrofit.create(TestRailService.class);

    Map<String, String> data = new HashMap<>();
    data.put(CustomQuery, "");
    data.put("created_after", timestamp);

    return service.getTestRuns(data).execute().body();
  }

  public List<TestCase> getAutomatedTestCases(String projectId) throws IOException {
    String CustomQuery = String.format(TestRailService.GET_TEST_CASES_API, projectId);
    TestRailService service = retrofit.create(TestRailService.class);

    Map<String, String> data = new HashMap<>();
    data.put(CustomQuery, "");
    data.put(TestCase.TYPE_ID, TestCase.TYPE_AUTOMATED);

    return service.getTestCases(data).execute().body();
  }

  public TestRun getTestRun(String testRunId) throws IOException {
    String CustomQuery = String.format(TestRailService.GET_TEST_RUN_API, testRunId);
    TestRailService service = retrofit.create(TestRailService.class);

    Map<String, String> data = new HashMap<>();
    data.put(CustomQuery, "");
    return service.getTestRun(data).execute().body();
  }

  public TestRun addTestRun(
      String projectId, String testRunName, List<Integer> includeTestCaseIds) throws IOException {
    if (includeTestCaseIds == null) {
      includeTestCaseIds = new ArrayList<>();
    }

    AddTestRunRequest request =
        new AddTestRunRequest(testRunName, includeTestCaseIds.size() == 0, includeTestCaseIds);

    String CustomQuery = String.format(TestRailService.ADD_TEST_RUN_API, projectId);
    TestRailService service = retrofit.create(TestRailService.class);

    Map<String, String> data = new HashMap<>();
    data.put(CustomQuery, "");
    TestRun testRun = service.addTestRun(data, request).execute().body();

    testRun.setTestCaseIds(includeTestCaseIds);

    return testRun;
  }

  public TestRun updateTestRun(TestRun testRun) throws IOException {

    AddTestRunRequest request =
        new AddTestRunRequest(testRun.getName(), testRun.getIncludeAll(), testRun.getTestCaseIds());

    String CustomQuery = String.format(TestRailService.UPDATE_TEST_RUN_API, testRun.getId());
    TestRailService service = retrofit.create(TestRailService.class);

    Map<String, String> data = new HashMap<>();
    data.put(CustomQuery, "");
    TestRun updatedTestRun = service.updateTestRun(data, request).execute().body();
    updatedTestRun.setTestCaseIds(testRun.getTestCaseIds());

    return updatedTestRun;
  }

  public void addTestResult(Integer testRunId, Integer testCaseId, AddTestResultRequest request)
      throws IOException {

    String CustomQuery =
        String.format(TestRailService.ADD_RESULT_FOR_TEST_CASE_API, testRunId, testCaseId);
    TestRailService service = retrofit.create(TestRailService.class);

    Map<String, String> data = new HashMap<>();
    data.put(CustomQuery, "");
    service.addResultForTestCase(data, request).execute().body();
  }

  public Attachment addAttachmentToTestRun(Integer testRunId, String imagePath) throws IOException {

    String CustomQuery = String.format(TestRailService.ADD_ATTACHMENT_FOR_TEST_RUN_API, testRunId);
    TestRailService service = retrofit.create(TestRailService.class);

    Map<String, String> data = new HashMap<>();
    data.put(CustomQuery, "");

    File file = new File(imagePath);
    RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
    MultipartBody.Part imageToUpload =
        MultipartBody.Part.createFormData("attachment", file.getName(), requestFile);

    return service.addAttachmentToTestRun(data, imageToUpload).execute().body();
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
