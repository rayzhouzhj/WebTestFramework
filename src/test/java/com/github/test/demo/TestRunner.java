package com.github.test.demo;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.scmp.framework.executor.TestExecutor;

public class TestRunner
{
    @Test
    public static void testApp() throws Exception 
    {
        TestExecutor parallelThread = new TestExecutor();
        List<String> tests = new ArrayList<>();
        tests.add("WebTest");
        boolean hasFailures = parallelThread.runner("com.github.test.demo", tests);
        
        Assert.assertFalse(hasFailures, "Testcases execution failed.");
    }
}
