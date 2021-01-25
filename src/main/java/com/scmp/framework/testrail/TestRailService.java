package com.scmp.framework.testrail;

import com.scmp.framework.testrail.models.TestRun;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;
import java.util.Map;

public interface TestRailService {

    String GET_TEST_RUNS_API = "/api/v2/get_runs/%s";

    @GET("index.php")
    @Headers({"Content-Type: application/json"})
    Call<List<TestRun>> getRuns(@QueryMap(encoded = true) Map<String, String> options);
}
