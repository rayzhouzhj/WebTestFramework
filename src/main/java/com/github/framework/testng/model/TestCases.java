package com.github.framework.testng.model;

import java.util.List;

public class TestCases 
{

    private String testCase;
    private List<TestMethods> testMethod;

    public String getTestCase() 
    {
        return testCase;
    }

    public void setTestCase(String testCase) 
    {
        this.testCase = testCase;
    }

    public List<TestMethods> getTestMethod() 
    {
        return testMethod;
    }

    public void setTestMethod(List<TestMethods> testMethod) 
    {
        this.testMethod = testMethod;
    }



}
