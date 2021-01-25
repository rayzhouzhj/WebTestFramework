package com.scmp.framework.testng.listeners;

import com.scmp.framework.context.RunTimeContext;
import com.scmp.framework.testrail.TestRailManager;
import com.scmp.framework.utils.ConfigFileKeys;
import org.testng.ISuite;
import org.testng.ISuiteListener;

public class SuiteListener implements ISuiteListener {
  @Override
  public void onFinish(ISuite suite) {
    System.out.println("onFinish function started of ISuiteListener ");
  }

  @Override
  public void onStart(ISuite suite) {
    if ("true"
        .equalsIgnoreCase(
            RunTimeContext.getInstance()
                .getProperty(ConfigFileKeys.TESTRAIL_UPLOAD_FLAG, "false"))) {

        initTestRail();
    }
  }

  public void initTestRail() {
    String baseUrl = RunTimeContext.getInstance().getProperty(ConfigFileKeys.TESTRAIL_SERVER);
    String userName = RunTimeContext.getInstance().getProperty(ConfigFileKeys.TESTRAIL_USER_NAME);
    String password = RunTimeContext.getInstance().getProperty(ConfigFileKeys.TESTRAIL_API_KEY);

    TestRailManager.init(baseUrl, userName, password);
  }
}
