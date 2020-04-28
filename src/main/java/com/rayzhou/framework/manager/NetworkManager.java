package com.rayzhou.framework.manager;

import com.rayzhou.framework.model.GoogleAnalystics;
import org.json.JSONObject;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class NetworkManager {

    public static void clearNetworkTraffic() {
        getAllNetworkTraffic();
    }

    public static List<String> getAllNetworkTraffic() {
        List<String> messages = new ArrayList<>();

        LogEntries logs = WebDriverManager.getDriver().manage().logs().get("performance");
        for (Iterator<LogEntry> it = logs.iterator(); it.hasNext(); ) {
            LogEntry entry = it.next();
            messages.add(entry.getMessage());
        }

        return messages;
    }

    public static List<GoogleAnalystics> getGoogleAnalyticsRequests() {
        List<GoogleAnalystics> gaData = Collections.synchronizedList(new ArrayList<>());

        LogEntries logs = WebDriverManager.getDriver().manage().logs().get("performance");
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

                        if (url.startsWith("https://www.google-analytics.com/collect?")
                                || url.startsWith("https://www.google-analytics.com/r/collect?")) {
                            System.out.println(entry.getMessage());
                            gaData.add(new GoogleAnalystics(url));
                        }
                    }
                });

        return gaData;
    }
}
