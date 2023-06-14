package com.scmp.framework.testrail.models;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

public class TestCase {
	public static String TYPE_ID = "type_id";
	public static String TYPE_AUTOMATED = "3";

	@SerializedName("id")
	private @Getter Integer id;

	@SerializedName("title")
	private @Getter String title;

	@SerializedName("section_id")
	private @Getter Integer sectionId;

	@SerializedName("type_id")
	private @Getter Integer typeId;

	@SerializedName("priority_id")
	private @Getter Integer priorityId;
}
