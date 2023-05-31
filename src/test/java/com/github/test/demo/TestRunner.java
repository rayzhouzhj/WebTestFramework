package com.github.test.demo;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.scmp.framework.executor.TestExecutor;

@SpringBootTest(classes = com.scmp.framework.TestFramework.class)
public class TestRunner extends AbstractTestNGSpringContextTests {

	@Autowired
	private TestExecutor testExecutor;

	@Test
	public void testApp() throws Exception {
		List<String> tests = new ArrayList<>();
		tests.add("APITest");

		List<String> packages = new ArrayList<>();
		packages.add("com.github.test.demo");
		boolean hasFailures = testExecutor.runTests(packages, tests);

		Assert.assertFalse(hasFailures, "Testcases execution failed.");
	}
}
