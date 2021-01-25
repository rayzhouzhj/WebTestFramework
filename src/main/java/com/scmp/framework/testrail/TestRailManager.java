package com.scmp.framework.testrail;

import com.scmp.framework.testrail.models.TestRun;
import okhttp3.*;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
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
    List<TestRun> runs = null;
    try {
      Map<String, String> data = new HashMap<>();
      data.put(CustomQuery, "");
      runs = service.getRuns(data).execute().body();
    } catch (IOException e) {
      e.printStackTrace();
    }
    System.out.println(runs.get(0));

    return runs;
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
