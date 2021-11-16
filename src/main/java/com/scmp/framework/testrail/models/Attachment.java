package com.scmp.framework.testrail.models;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Data
public class Attachment {

    public static transient String ATTACHMENT_REF_STRING = "![](index.php?/attachments/get/%s)";

    @SerializedName("attachment_id")
    private String attachmentId;
}
