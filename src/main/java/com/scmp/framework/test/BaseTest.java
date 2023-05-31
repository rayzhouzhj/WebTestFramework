package com.scmp.framework.test;

import com.scmp.framework.context.ApplicationContextProvider;
import com.scmp.framework.context.RunTimeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.Random;

public class BaseTest {
	private static final Logger frameworkLogger = LoggerFactory.getLogger(BaseTest.class);
	protected final TestLogger logger;

	public BaseTest() {
		ApplicationContext context = ApplicationContextProvider.getApplicationContext();
		logger = context.getBean(TestLogger.class);
	}

	public String getRandomNumberString(int length) {
		StringBuilder output = new StringBuilder();
		Random random = new Random();

		for (int i = 0; i < length; i++) {
			output.append(random.nextInt(10));
		}

		frameworkLogger.info("Generated random Number String: {}", output.toString());
		return output.toString();
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
