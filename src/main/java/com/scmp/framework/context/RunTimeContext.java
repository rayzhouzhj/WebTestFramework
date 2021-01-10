package com.scmp.framework.context;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

import com.scmp.framework.utils.ConfigFileKeys;
import com.scmp.framework.utils.ConfigFileReader;

public class RunTimeContext {
  private static RunTimeContext instance;
  private ConcurrentHashMap<String, Object> concurrentHashMap = new ConcurrentHashMap<>();

  private RunTimeContext() {}

  public static synchronized RunTimeContext getInstance() {
    if (instance == null) {
      instance = new RunTimeContext();
    }

    return instance;
  }

  public Object getVariables(String name) {
    return this.concurrentHashMap.getOrDefault(name, null);
  }

  public Object setVariables(String name, Object data) {
    return this.concurrentHashMap.put(name, data);
  }

  public String getProperty(String name) {
    return this.getProperty(name, null);
  }

  public String getProperty(String key, String defaultValue) {
    String value = System.getenv(key);
    if (value == null || value.isEmpty()) {
      value = ConfigFileReader.getInstance().getProperty(key, defaultValue);
    }

    return value;
  }

  public String getURL() {
    return this.getProperty("URL", "");
  }

  public synchronized String getLogPath(String category, String className, String methodName) {
    String path =
        System.getProperty("user.dir")
            + File.separator
            + "target"
            + File.separator
            + category
            + File.separator
            + className
            + File.separator
            + methodName;

    File file = new File(path);
    if (!file.exists()) {
      if (file.mkdirs()) {
        System.out.println("Directory [" + path + "] is created!");
      } else {
        System.out.println("Failed to create directory!");
      }
    }

    return path;
  }

  public boolean isLocalExecutionMode() {
    String isDebugMode = this.getProperty(ConfigFileKeys.DEBUG_MODE, "OFF");
    String isLocalExecutionMode = this.getProperty(ConfigFileKeys.LOCAL_EXECUTION, "OFF");

    return "ON".equalsIgnoreCase(isLocalExecutionMode) || "ON".equalsIgnoreCase(isDebugMode);
  }

  public boolean removeFailedTestB4Retry() {
    return "true"
        .equalsIgnoreCase(this.getProperty(ConfigFileKeys.REMOVE_FAILED_TEST_B4_RETRY, "false"));
  }
}
