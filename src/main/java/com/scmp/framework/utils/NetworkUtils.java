package com.scmp.framework.utils;

import com.scmp.framework.model.ChartbeatData;
import com.scmp.framework.model.GoogleAnalytics;
import org.json.JSONObject;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NetworkUtils {

	public static void clearNetworkTraffic(RemoteWebDriver driver) {
		getAllNetworkTraffic(driver);
	}

	public static List<String> getAllNetworkTraffic(RemoteWebDriver driver) {
		List<String> messages = new ArrayList<>();

		LogEntries logs = driver.manage().logs().get("performance");
		for (Iterator<LogEntry> it = logs.iterator(); it.hasNext(); ) {
			LogEntry entry = it.next();
			messages.add(entry.getMessage());
		}

		return messages;
	}

	private static <T> List<T> getTrackingRequests(RemoteWebDriver driver, Class<T> cls, String pattern) {
		List<T> trackingData = Collections.synchronizedList(new ArrayList<>());
		// Create a Pattern object
		Pattern regex = Pattern.compile(pattern);

		LogEntries logs = driver.manage().logs().get("performance");
		logs.getAll()
				.parallelStream()
				.forEach(entry -> {
					JSONObject json = new JSONObject(entry.getMessage());
					JSONObject message = (JSONObject) json.get("message");
					String method = (String) message.get("method");
					if (method.equals("Network.requestWillBeSent")) {
						JSONObject params = (JSONObject) message.get("params");
						JSONObject request = (JSONObject) params.get("request");
						String url = (String) request.get("url");

						Matcher matcher = regex.matcher(url);
						if (matcher.matches()) {
							System.out.println(entry.getMessage());
							try {
								trackingData.add(cls.getConstructor(String.class).newInstance(url));
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				});

		return trackingData;
	}

	public static List<GoogleAnalytics> getGoogleAnalyticsRequests(RemoteWebDriver driver) {
		List<GoogleAnalytics> gaData = Collections.synchronizedList(new ArrayList<>());
		String pattern = "^https://www.google-analytics.com/([a-z]/)?collect\\?.+";
		return getTrackingRequests(driver, GoogleAnalytics.class, pattern);
	}

	public static List<ChartbeatData> getChartBeatRequests(RemoteWebDriver driver) {
		List<GoogleAnalytics> gaData = Collections.synchronizedList(new ArrayList<>());
		String pattern = "^https://ping.chartbeat.net/ping\\?.+";
		return getTrackingRequests(driver, ChartbeatData.class, pattern);
	}
}