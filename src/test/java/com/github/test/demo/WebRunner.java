package com.github.test.demo;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.rayzhou.framework.executor.TestExecutor;

public class WebRunner 
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
