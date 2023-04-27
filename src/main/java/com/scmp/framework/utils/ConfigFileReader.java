package com.scmp.framework.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * ConfigFileManager - Read config file statically into configFileMap
 */
public class ConfigFileReader
{
	private Map<String, String> configFileMap = new HashMap<>();
	private Properties prop = new Properties();

	public ConfigFileReader(String configFile)
	{
		try {
			FileInputStream inputStream = new FileInputStream(configFile);
			prop.load(inputStream);
			Enumeration<?> keys = prop.propertyNames();
			while (keys.hasMoreElements())
			{
				String key = (String) keys.nextElement();
				configFileMap.put(key, prop.getProperty(key));
			}
		} catch (Exception e) {
			System.err.println(e);
			System.err.println("Failed to read config data from [" + configFile + "]");
		}
	}

	public Map<String, String> getAllProperties()
	{
		return configFileMap;
	}

	public String getProperty(String object) 
	{
		return configFileMap.get(object);
	}

	public String getProperty(String key, String value) 
	{
		return configFileMap.getOrDefault(key, value);
	}
}
