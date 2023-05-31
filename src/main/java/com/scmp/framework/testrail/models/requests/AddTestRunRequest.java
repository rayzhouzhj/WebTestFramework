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
	private String name;

	@SerializedName("include_all")
	private Boolean includeAll;

	@SerializedName("case_ids")
	private List<Integer> testCaseIds;
}
