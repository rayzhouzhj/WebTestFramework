package com.scmp.framework.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class BaseTest {
  private static final Logger frameworkLogger = LoggerFactory.getLogger(BaseTest.class);
  protected TestLogger logger = new TestLogger();

  public String getRandomNumberString(int length) {
    String output = "";
    Random random = new Random();

    for (int i = 0; i < length; i++) {
      output = output + random.nextInt(10);
    }

    frameworkLogger.info("Generated random Number String: {}", output);
    return output;
  }

  public void sleep(long millis) {
    try {
      frameworkLogger.info("Wait for " + millis + " milliseconds");
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      frameworkLogger.error("Ops!", e);
    }
  }
}
