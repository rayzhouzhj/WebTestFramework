package com.scmp.framework.testrail.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TestResult {
    @SerializedName("id")
    private int id;

    @SerializedName("test_id")
    private int testId;

    @SerializedName("status_id")
    private int statusId;

    @SerializedName("comment")
    private String comment;

    @SerializedName("elapsed")
    private String elapsed;

    @SerializedName("custom_step_results")
    private List<CustomStepResult> customStepResult;
}
