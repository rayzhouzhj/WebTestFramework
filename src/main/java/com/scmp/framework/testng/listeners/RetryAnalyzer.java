package com.scmp.framework.testng.listeners;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import com.scmp.framework.TestFramework;
import com.scmp.framework.annotations.RetryCount;
import com.scmp.framework.context.ApplicationContextProvider;
import com.scmp.framework.testng.model.RetryMethod;
import com.scmp.framework.utils.ConfigFileKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import com.scmp.framework.context.RunTimeContext;

public class RetryAnalyzer implements IRetryAnalyzer {
	private static final Logger frameworkLogger = LoggerFactory.getLogger(RetryAnalyzer.class);
	private final ConcurrentHashMap<String, RetryMethod> retryMap = new ConcurrentHashMap<>();

	private final RunTimeContext runTimeContext;

	public RetryAnalyzer() {
		ApplicationContext context = ApplicationContextProvider.getApplicationContext();
		runTimeContext = context.getBean(RunTimeContext.class);
	}

	public boolean isRetriedMethod(ITestResult iTestResult) {
		return this.getRetryMethod(iTestResult).isRetried();
	}

	public boolean isRetriedRequired(ITestResult iTestResult) {
		return this.getRetryMethod(iTestResult).needRetry();
	}

	@Override
	public boolean retry(ITestResult iTestResult) {
		if (iTestResult.getStatus()==ITestResult.FAILURE) {
			RetryMethod method = getRetryMethod(iTestResult);
			frameworkLogger.error("Test Failed - " + method.getMethodName());
			if (method.needRetry()) {
				method.increaseRetryCount();
				frameworkLogger.info(
						"Retrying Failed Test Cases "
								+ method.getRetryCount()
								+ " out of "
								+ method.getMaxRetryCount());

				return true;
			} else {
				frameworkLogger.info("Meet maximum retry count [ " + method.getMaxRetryCount() + " ]");

				return false;
			}
		}

		return false;
	}

	public RetryMethod getRetryMethod(ITestResult iTestResult) {
		String methodName = iTestResult.getMethod().getMethodName();
		String key = methodName + Thread.currentThread().getId();

		if (this.retryMap.containsKey(key)) {
			return this.retryMap.get(key);
		} else {
			int maxRetryCount = 0;
			Method[] methods = iTestResult.getInstance().getClass().getMethods();
			for (Method m : methods) {
				if (m.getName().equals(methodName)) {
					if (m.isAnnotationPresent(RetryCount.class)) {
						RetryCount ta = m.getAnnotation(RetryCount.class);
						maxRetryCount = ta.maxRetryCount();
					} else {
						try {
							maxRetryCount =
									Integer.parseInt(
											runTimeContext.getProperty(ConfigFileKeys.MAX_RETRY_COUNT));
						} catch (Exception e) {
							maxRetryCount = 0;
						}
					}

					break;
				}
			}

			this.retryMap.put(key, new RetryMethod(0, maxRetryCount, methodName));

			return this.retryMap.get(key);
		}
	}
}
