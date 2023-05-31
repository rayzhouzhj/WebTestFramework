package com.scmp.framework.testrail.models;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomStepResult {
	@SerializedName("content")
	private String content;

	@SerializedName("status_id")
	private int statusId;
}