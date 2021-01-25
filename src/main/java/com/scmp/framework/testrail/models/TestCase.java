package com.scmp.framework.testrail.models;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

public class TestCase {
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
