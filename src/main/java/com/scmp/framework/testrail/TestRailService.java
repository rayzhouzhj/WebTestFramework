package com.scmp.framework.testrail;

import com.scmp.framework.testrail.models.Attachment;
import com.scmp.framework.testrail.models.TestCase;
import com.scmp.framework.testrail.models.TestResult;
import com.scmp.framework.testrail.models.TestRun;
import com.scmp.framework.testrail.models.requests.AddTestResultRequest;
import com.scmp.framework.testrail.models.requests.AddTestRunRequest;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;
import java.util.Map;

public interface TestRailService {

    String GET_TEST_RUN_API = "/api/v2/get_run/%s";
    String GET_TEST_RUNS_API = "/api/v2/get_runs/%s";
    String GET_TEST_CASES_API = "/api/v2/get_cases/%s";

    String ADD_TEST_RUN_API = "/api/v2/add_run/%s";
    String UPDATE_TEST_RUN_API = "/api/v2/update_run/%s";

    String ADD_RESULT_FOR_TEST_CASE_API = "/api/v2/add_result_for_case/%s/%s";
    String ADD_ATTACHMENT_FOR_TEST_RUN_API = "/api/v2/add_attachment_to_run/%s";

    @GET("index.php")
    @Headers({"Content-Type: application/json"})
    Call<TestRun> getTestRun(@QueryMap(encoded = true) Map<String, String> options);

    @GET("index.php")
    @Headers({"Content-Type: application/json"})
    Call<List<TestRun>> getTestRuns(@QueryMap(encoded = true) Map<String, String> options);

    @GET("index.php")
    @Headers({"Content-Type: application/json"})
    Call<List<TestCase>> getTestCases(@QueryMap(encoded = true) Map<String, String> options);

    @POST("index.php")
    @Headers({"Content-Type: application/json"})
    Call<TestRun> addTestRun(@QueryMap(encoded = true) Map<String, String> options, @Body AddTestRunRequest request);

    @POST("index.php")
    @Headers({"Content-Type: application/json"})
    Call<TestRun> updateTestRun(@QueryMap(encoded = true) Map<String, String> options, @Body AddTestRunRequest request);

    @POST("index.php")
    @Headers({"Content-Type: application/json"})
    Call<TestResult> addResultForTestCase(@QueryMap(encoded = true) Map<String, String> options, @Body AddTestResultRequest request);

    @POST("index.php")
    @Multipart
    Call<Attachment> addAttachmentToTestRun(@QueryMap(encoded = true) Map<String, String> options, @Part MultipartBody.Part image);
}
