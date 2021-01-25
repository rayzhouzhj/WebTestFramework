package com.scmp.framework.testrail.models;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

public class TestRun {

    @SerializedName("id")
    private @Getter int id;

    @SerializedName("name")
    private @Getter String name;
}
