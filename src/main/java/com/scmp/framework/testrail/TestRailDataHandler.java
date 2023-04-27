package com.scmp.framework.testrail;

import com.scmp.framework.testrail.models.Attachment;
import com.scmp.framework.testrail.models.CustomStepResult;
import com.scmp.framework.testrail.models.TestResult;
import com.scmp.framework.testrail.models.TestRun;
import com.scmp.framework.testrail.models.requests.AddTestResultRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class TestRailDataHandler {
  private static final Logger frameworkLogger = LoggerFactory.getLogger(TestRailDataHandler.class);

  private int testcaseId;
  private TestRun testRun;
  private boolean isTestResultForUploadAttachmentsReady = false;
  private TestResult testResultForUploadAttachments;
  private List<CustomStepResult> testRailCustomStepResultList = new ArrayList<>();
  private ConcurrentLinkedQueue<CustomStepResult> pendingTaskQueue = new ConcurrentLinkedQueue<>();

  public TestRailDataHandler(int testcaseId, TestRun testRun) {
    this.testcaseId = testcaseId;
    this.testRun = testRun;

    this.initTestResultForUploadAttachments();
  }

  private void initTestResultForUploadAttachments() {
    new Thread(
            () -> {
              // Create a new test result for adding attachment
              String comment = "Mark In Progress Status";
              AddTestResultRequest request =
                  new AddTestResultRequest(TestRailStatus.IN_PROGRESS, comment, "", new ArrayList<>());
              try {
                this.testResultForUploadAttachments =
                    TestRailManager.getInstance()
                        .addTestResult(this.testRun.getId(), this.testcaseId, request);

                this.isTestResultForUploadAttachmentsReady = true;
              } catch (IOException e) {
                frameworkLogger.error("Failed to create test result.", e);
              }
            })
        .start();
  }

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
      pendingTaskQueue.add(stepResult);

      Thread updateStepWithAttachment =
          new Thread(
              () -> {
                try {
                  // Wait for test result for attachment ready
                  int maxWaitSeconds = 20;
                  int seconds = 0;
                  while (!this.isTestResultForUploadAttachmentsReady && seconds < maxWaitSeconds) {
                    try {
                      seconds++;
                      TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                      frameworkLogger.error("Ops!", e);
                    }
                  }

                  frameworkLogger.info("Uploading attachment: " + filePath);
                  Attachment attachment =
                      TestRailManager.getInstance()
                          .addAttachmentToTestResult(
                              testResultForUploadAttachments.getId(), filePath);

                  String attachmentRef =
                      String.format(Attachment.ATTACHMENT_REF_STRING, attachment.getAttachmentId());
                  frameworkLogger.info("Attachment uploaded: " + attachmentRef);
                  stepResult.setContent(stepResult.getContent() + " \n " + attachmentRef);
                } catch (Exception e) {
                  frameworkLogger.error("Failed to upload attachment.", e);
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
        frameworkLogger.error("Ops!", e);
      }
    }

//    frameworkLogger.info("========RESULT CONTENT==========");
//    testRailCustomStepResultList.forEach(result -> {frameworkLogger.info(result.getContent());});
//    frameworkLogger.info("================================");

    AddTestResultRequest request =
        new AddTestResultRequest(
            finalTestResult, "", elapsedInSecond + "s", testRailCustomStepResultList);
    try {
      TestRailManager.getInstance().addTestResult(this.testRun.getId(), this.testcaseId, request);
    } catch (IOException e) {
      frameworkLogger.error("Failed to create test result.", e);
    }
  }
}
