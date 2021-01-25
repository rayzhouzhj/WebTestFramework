package com.scmp.framework.testrail.models;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Data
public class Attachment {

    @SerializedName("attachment_id")
    private int attachmentId;
}
