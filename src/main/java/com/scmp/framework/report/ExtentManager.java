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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.scmp.framework.utils.Constants.TARGET_PATH;

@Component
public class ExtentManager {
	private static final Logger frameworkLogger = LoggerFactory.getLogger(ExtentManager.class);

	private static ExtentReports extent;
	private static final String filePath = TARGET_PATH + File.separator + "WebTestReport.html";
	private final RunTimeContext runTimeContext;

	@Autowired
	public ExtentManager(RunTimeContext runTimeContext) {
		this.runTimeContext = runTimeContext;
	}

	public synchronized ExtentReports getExtent() {
		if (extent==null) {
			extent = new ExtentReports();
			extent.attachReporter(getHtmlReporter());

			String browserType = runTimeContext.getFrameworkConfigs().getBrowserType();
			int threadCount = runTimeContext.getFrameworkConfigs().getThreadCount();
			String excludeGroups = runTimeContext.getFrameworkConfigs().getExcludeGroups();
			String includeGroups = runTimeContext.getFrameworkConfigs().getIncludeGroups();
			String url = runTimeContext.getFrameworkConfigs().getUrl();
			String featureDesc = runTimeContext.getFrameworkConfigs().getFeatureDescription();

			extent.setSystemInfo("URL", url);
			extent.setSystemInfo("Include Groups", includeGroups);
			extent.setSystemInfo("Exclude Groups", excludeGroups);
			extent.setSystemInfo("Browser Type", browserType);
			extent.setSystemInfo("Tread Count", String.valueOf(threadCount));
			extent.setSystemInfo("Feature", featureDesc);

			List<Status> statusHierarchy =
					Arrays.asList(
							Status.FATAL,
							Status.FAIL,
							Status.ERROR,
							Status.WARNING,
							Status.PASS,
							Status.SKIP,
							Status.DEBUG,
							Status.INFO);

			extent.config().statusConfigurator().setStatusHierarchy(statusHierarchy);
		}

		return extent;
	}

	private ExtentHtmlReporter getHtmlReporter() {
		ExtentHtmlReporter htmlReporter = new ExtentHtmlReporter(filePath);
		String extentXML = runTimeContext.getFrameworkConfigs().getExtentXMLPath();

		boolean loadDefaultConfig = true;
		if (extentXML!=null && !extentXML.isEmpty()) {
			frameworkLogger.info("Loading extent.xml from {}", extentXML);
			try {
				htmlReporter.loadXMLConfig(extentXML);
				loadDefaultConfig = false;
			} catch (Exception e) {
				frameworkLogger.error("Failed to load extent.xml from " + extentXML, e);
			}
		}

		if (loadDefaultConfig) {
			frameworkLogger.info("Using default extent configs.");

			// report title
			htmlReporter.config().setDocumentTitle("WEB Test Report");
			htmlReporter.config().setReportName("WEB Test Report");
			htmlReporter.config().setTestViewChartLocation(ChartLocation.TOP);
			htmlReporter.config().setTheme(Theme.STANDARD);
		}

		// make the charts visible on report open
		htmlReporter.config().setChartVisibilityOnOpen(true);

		return htmlReporter;
	}
}
