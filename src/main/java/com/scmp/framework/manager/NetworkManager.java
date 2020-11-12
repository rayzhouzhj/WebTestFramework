package com.scmp.framework.manager;

import com.scmp.framework.model.GoogleAnalystics;
import org.json.JSONObject;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        String pattern = "^https://www.google-analytics.com/([a-z]/)?collect\\?.+";
        // Create a Pattern object
        Pattern regex = Pattern.compile(pattern);

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

                        Matcher matcher = regex.matcher(url);
                        if (matcher.matches()) {
                            System.out.println(entry.getMessage());
                            gaData.add(new GoogleAnalystics(url));
                        }
                    }
                });

        return gaData;
    }
}
