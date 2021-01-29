package com.scmp.framework.testrail;

import com.scmp.framework.testrail.models.Attachment;
import com.scmp.framework.testrail.models.CustomStepResult;
import com.scmp.framework.testrail.models.TestResult;
import com.scmp.framework.testrail.models.TestRun;
import com.scmp.framework.testrail.models.requests.AddTestResultRequest;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class TestRailDataHandler {

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
              // Get the 1st test result for re-run test
              try {
                List<TestResult> testResultList =
                    TestRailManager.getInstance()
                        .getTestResultsForTestCase(this.testRun.getId(), this.testcaseId);

                if (testResultList.size() > 0) {
                  this.testResultForUploadAttachments = testResultList.get(testResultList.size() - 1);
                  this.isTestResultForUploadAttachmentsReady = true;
                  return;
                }
              } catch (IOException e) {
                e.printStackTrace();
              }

              // Create a new test result for adding attachment
              String comment = "Init test comment for attaching images";
              AddTestResultRequest request =
                  new AddTestResultRequest(TestRailStatus.Retest, comment, "", new ArrayList<>());
              try {
                this.testResultForUploadAttachments =
                    TestRailManager.getInstance()
                        .addTestResult(this.testRun.getId(), this.testcaseId, request);

                this.isTestResultForUploadAttachmentsReady = true;
              } catch (IOException e) {
                e.printStackTrace();
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
                      e.printStackTrace();
                    }
                  }

                  System.out.println("Uploading attachment: " + filePath);
                  Attachment attachment =
                      TestRailManager.getInstance()
                          .addAttachmentToTestResult(
                              testResultForUploadAttachments.getId(), filePath);

                  String attachmentRef =
                      String.format(Attachment.ATTACHMENT_REF_STRING, attachment.getAttachmentId());
                  System.out.println("Attachment uploaded: " + attachmentRef);
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

    System.out.println("========RESULT CONTENT==========");
    testRailCustomStepResultList.forEach(result -> {System.out.println(result.getContent());});
    System.out.println("================================");

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
