package com.scmp.framework.testrail.models.requests;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Data
@AllArgsConstructor
public class AddTestRunRequest {

    @SerializedName("name")
    @NonNull
    private String name;

    @SerializedName("include_all")
    private Boolean includeAll;

    @SerializedName("case_ids")
    @NonNull
    private List<Integer> testCaseIds;
}
