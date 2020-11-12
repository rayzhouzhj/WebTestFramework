package com.scmp.framework.report;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.configuration.ChartLocation;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.scmp.framework.context.RunTimeContext;

public class ExtentManager 
{
	private static ExtentReports extent;
	private static String filePath = System.getProperty("user.dir") + File.separator + "target" + File.separator + "WebTestReport.html";
	private static String extentXML = System.getProperty("user.dir") + File.separator + "extent.xml";

	public synchronized static ExtentReports getExtent() 
	{
		if (extent == null)
		{
			extent = new ExtentReports();
			extent.attachReporter(getHtmlReporter());

			String executionMode = RunTimeContext.getInstance().getProperty("RUNNER");
			String platform = RunTimeContext.getInstance().getProperty("Platform");
			String build = RunTimeContext.getInstance().getProperty("BuildNumber");
			if(build == null) build = "";
			
			extent.setSystemInfo("Runner", executionMode);
			extent.setSystemInfo("Platform", platform);
			extent.setSystemInfo("Build", build);

			List<Status> statusHierarchy = Arrays.asList(
					Status.FATAL,
					Status.FAIL,
					Status.ERROR,
					Status.WARNING,
					Status.PASS,
					Status.SKIP,
					Status.DEBUG,
					Status.INFO
					);

			extent.config().statusConfigurator().setStatusHierarchy(statusHierarchy);
		}

		return extent;
	}

	private static ExtentHtmlReporter getHtmlReporter()
	{
		ExtentHtmlReporter htmlReporter = new ExtentHtmlReporter(filePath);
		htmlReporter.loadXMLConfig(extentXML);
		// make the charts visible on report open
		htmlReporter.config().setChartVisibilityOnOpen(true);

		// report title
		htmlReporter.config().setDocumentTitle("WEB Test Report");
		htmlReporter.config().setReportName("WEB Test Report");
		htmlReporter.config().setTestViewChartLocation(ChartLocation.TOP);
		htmlReporter.config().setTheme(Theme.STANDARD);

		return htmlReporter;
	}

	public static void flush()
	{
		extent.flush();
	}
}
