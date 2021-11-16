package com.scmp.framework.testrail.models;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import java.util.List;

@Data
public class TestRunTestResult {

    @SerializedName("tests")
    List<TestRunTest> TestRunTestList;
}
