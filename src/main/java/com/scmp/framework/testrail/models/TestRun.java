package com.scmp.framework.testrail.models;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Data
public class TestRun {

    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("include_all")
    private Boolean includeAll;

    private transient List<Integer> testCaseIds;
}
