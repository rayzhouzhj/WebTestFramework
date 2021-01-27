package com.scmp.framework.testrail;

import com.scmp.framework.testrail.models.Attachment;
import com.scmp.framework.testrail.models.CustomStepResult;
import com.scmp.framework.testrail.models.TestRun;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Data
@RequiredArgsConstructor
public class TestRailDataHandler {

    private @NonNull int id;
    private @NonNull TestRun testRun;
    private List<CustomStepResult> testRailCustomStepResultList = new ArrayList<>();
    private ConcurrentLinkedQueue<CustomStepResult> pendingTaskQueue = new ConcurrentLinkedQueue<>();

    public void addStepResult(int status, String content, String filePath) {
        final CustomStepResult stepResult = new CustomStepResult(content, status);
        testRailCustomStepResultList.add(stepResult);

        if(filePath != null) {
            Thread updateStepWithAttachment = new Thread(() -> {
                try {
                    pendingTaskQueue.add(stepResult);

                    Attachment attachment = TestRailManager.getInstance().addAttachmentToTestRun(testRun.getId(), filePath);
                    String attachmentRef = String.format(Attachment.ATTACHMENT_REF_STRING, attachment.getAttachmentId());
                    stepResult.setContent(stepResult.getContent() + "\n" + attachmentRef);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    pendingTaskQueue.remove(stepResult);
                }
            });

            updateStepWithAttachment.start();
        }

    }
}
