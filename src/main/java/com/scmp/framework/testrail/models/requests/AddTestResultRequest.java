package com.scmp.framework.testrail.models.requests;

import com.google.gson.annotations.SerializedName;
import com.scmp.framework.testrail.models.CustomStepResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddTestResultRequest {

	@SerializedName("status_id")
	private int statusId;

	@SerializedName("comment")
	private String comment;

	@SerializedName("elapsed")
	private String elapsed;

	@SerializedName("custom_step_results")
	private List<CustomStepResult> customStepResult;
}
