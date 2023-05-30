package com.github.test.demo;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.scmp.framework.executor.TestExecutor;

@SpringBootTest
public class TestRunner {

	@Autowired
	private final TestExecutor testExecutor;

	public TestRunner(TestExecutor testExecutor) {
		this.testExecutor = testExecutor;
	}

	@Test
	public void testApp() throws Exception {
		List<String> tests = new ArrayList<>();
		tests.add("WebTest");
		boolean hasFailures = testExecutor.runner("com.github.test.demo", tests);

		Assert.assertFalse(hasFailures, "Testcases execution failed.");
	}
}
