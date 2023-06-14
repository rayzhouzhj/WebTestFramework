package com.scmp.framework.testrail.models;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class TestRunTest {

	@SerializedName("id")
	private int id;

	@SerializedName("case_id")
	private int caseId;

	@SerializedName("status_id")
	private int statusId;

	@SerializedName("run_id")
	private int runId;

	@SerializedName("title")
	private String title;
}
