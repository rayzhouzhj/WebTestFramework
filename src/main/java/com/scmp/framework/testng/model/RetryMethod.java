package com.scmp.framework.testng.model;

import com.scmp.framework.model.Browser;

public class RetryMethod {
	private int retryCount = 0;
	private int maxRetryCount = 0;
	private String methodName = "";
	private Browser browserType = null;
	private boolean isRetried = false;

	public RetryMethod(int retryCount, int maxRetryCount, String methodName) {
		this.retryCount = retryCount;
		this.maxRetryCount = maxRetryCount;
		this.methodName = methodName;
	}

	public String getMethodName() {
		return this.methodName;
	}

	public int getRetryCount() {
		return this.retryCount;
	}

	public int getMaxRetryCount() {
		return this.maxRetryCount;
	}

	public boolean needRetry() {
		return retryCount < maxRetryCount;
	}

	public void increaseRetryCount() {
		isRetried = true;
		retryCount++;
	}

	public void decreaseRetryCount() {
		retryCount--;
	}

	public void setBrowserType(Browser browserType) {
		this.browserType = browserType;
	}

	public Browser getBrowserType() {
		return this.browserType;
	}

	public void setRetried(boolean status) {
		this.isRetried = status;
	}

	public boolean isRetried() {
		return this.isRetried;
	}
}
