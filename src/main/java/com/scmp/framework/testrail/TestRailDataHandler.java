package com.scmp.framework.testrail;

import com.scmp.framework.testrail.models.Attachment;
import com.scmp.framework.testrail.models.CustomStepResult;
import com.scmp.framework.testrail.models.TestRun;
import com.scmp.framework.testrail.models.requests.AddTestResultRequest;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

@Data
@RequiredArgsConstructor
public class TestRailDataHandler {

  private @NonNull int testcaseId;
  private @NonNull TestRun testRun;
  private List<CustomStepResult> testRailCustomStepResultList = new ArrayList<>();
  private ConcurrentLinkedQueue<CustomStepResult> pendingTaskQueue = new ConcurrentLinkedQueue<>();

  /**
   * Add Test Step Result
   *
   * @param status
   * @param content
   * @param filePath
   */
  public void addStepResult(int status, String content, String filePath) {
    final CustomStepResult stepResult = new CustomStepResult(content, status);
    testRailCustomStepResultList.add(stepResult);

    if (filePath != null) {
      Thread updateStepWithAttachment =
          new Thread(
              () -> {
                try {
                  pendingTaskQueue.add(stepResult);

                  Attachment attachment =
                      TestRailManager.getInstance()
                          .addAttachmentToTestRun(testRun.getId(), filePath);
                  String attachmentRef =
                      String.format(Attachment.ATTACHMENT_REF_STRING, attachment.getAttachmentId());
                  stepResult.setContent(stepResult.getContent() + " \n " + attachmentRef);
                } catch (Exception e) {
                  e.printStackTrace();
                } finally {
                  pendingTaskQueue.remove(stepResult);
                }
              });

      updateStepWithAttachment.start();
    }
  }

  /**
   * Upload data to Test Rail
   *
   * @param finalTestResult
   * @param elapsedInSecond
   */
  public void uploadDataToTestRail(int finalTestResult, long elapsedInSecond) {
    // Wait for pending tasks to complete
    int maxWaitSeconds = 20;
    int seconds = 0;
    while (pendingTaskQueue.size() > 0 && seconds < maxWaitSeconds) {
      try {
        seconds++;
        TimeUnit.SECONDS.sleep(1);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    AddTestResultRequest request =
        new AddTestResultRequest(
            finalTestResult, "", elapsedInSecond + "s", testRailCustomStepResultList);
    try {
      TestRailManager.getInstance().addTestResult(this.testRun.getId(), this.testcaseId, request);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
