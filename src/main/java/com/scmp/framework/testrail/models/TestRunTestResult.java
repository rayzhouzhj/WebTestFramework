package com.scmp.framework.testrail.models;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import java.util.List;

@Data
public class TestRunTestResult {

    @SerializedName("tests")
    List<TestRunTest> TestRunTestList;

    @SerializedName("offset")
    int offset;

    @SerializedName("limit")
    int limit;

    @SerializedName("size")
    int size;

    @SerializedName("_links")
    PagingLinks pagingLinks;
}
