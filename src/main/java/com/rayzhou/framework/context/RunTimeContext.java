package com.rayzhou.framework.context;

import java.io.File;

import com.rayzhou.framework.utils.ConfigFileReader;

public class RunTimeContext 
{
	private static RunTimeContext instance;
	
	private RunTimeContext()
	{
		
	} 
	
	public static synchronized RunTimeContext getInstance() 
	{
		if (instance == null) 
		{
			instance = new RunTimeContext();
		}
		
		return instance;
	}
	
	public boolean isParallelExecution()
	{
		return "parallel".equalsIgnoreCase(this.getProperty("RUNNER", "").trim());
	}
	
	public String getProperty(String name) 
	{
		return this.getProperty(name, null);
	}

	public String getProperty(String key, String defaultValue) 
	{
		String value = System.getenv(key);
		if(value == null || value.isEmpty())
		{
			value = ConfigFileReader.getInstance().getProperty(key, defaultValue);
		}
		
		return value;
	}

	public String getURL(){
		return this.getProperty("URL", "");
	}

	public synchronized String getLogPath(String category, String className, String methodName)
	{
		String path = System.getProperty("user.dir")
		+ File.separator + "target"
		+ File.separator + category
		+ File.separator + className 
		+ File.separator + methodName;
		
		File file = new File(path);
		if (!file.exists()) 
		{
			if (file.mkdirs()) 
			{
				System.out.println("Directory [" + path + "] is created!");
			} 
			else
			{
				System.out.println("Failed to create directory!");
			}
		}
		
		return path;
	}

	public boolean isDebugMode() {
		return "ON".equalsIgnoreCase(RunTimeContext.getInstance().getProperty("DEBUG_MODE", "OFF"));
	}

	public boolean removeFailedTestB4Retry() {
		return "true".equalsIgnoreCase(RunTimeContext.getInstance().getProperty("REMOVE_FAILED_TEST_B4_RETRY", "false"));
	}
}
