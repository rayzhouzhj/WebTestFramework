package com.github.framework.report;

import java.util.concurrent.ConcurrentHashMap;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;

public class ExtentTestManager
{
	private static ExtentReports extent = ExtentManager.getExtent();
	private static ConcurrentHashMap<String, ExtentTest> extentReportMap = new ConcurrentHashMap<>();

	public static void removeTest(ExtentTest test) 
	{
		extent.removeTest(test);
	}
	
	public synchronized static ExtentTest createTest(String name, String description) 
	{
		ExtentTest test;
		String testNodeName = name;
		
		if(extentReportMap.containsKey(testNodeName))
		{
			System.out.println("Reuse Test Thread ID: "+ Thread.currentThread().getId() + ", Key: " + name);
			test = extentReportMap.get(testNodeName);
		}
		else
		{
			System.out.println("Create new Test Thread ID: "+ Thread.currentThread().getId() + ", Key: " + name);
			test = extent.createTest(name, description);
			extentReportMap.put(testNodeName, test);
		}

		return test;
	}
}
