package com.scmp.framework.model;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractTrackingData {
	private String originalUrl = "";
	private Map<String, String> variables = new HashMap<>();

	public AbstractTrackingData(String originalUrl) {
		this.originalUrl = originalUrl;
		this.parse(originalUrl);
	}

	public String getOriginalUrl() {
		return this.originalUrl;
	}

	public Map<String, String> getVariables() {
		return this.variables;
	}

	public void parse(String url) {
		try {
			this.variables = this.splitQuery(new URL(url));
		} catch (Exception e) {
			throw new RuntimeException("Unable to parse URL: " + url);
		}
	}

	public Map<String, String> splitQuery(URL url) throws UnsupportedEncodingException {
		Map<String, String> query_pairs = new LinkedHashMap<String, String>();
		String query = url.getQuery();
		String[] pairs = query.split("&");
		for (String pair : pairs) {
			int idx = pair.indexOf("=");
			query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
		}
		return query_pairs;
	}
}
