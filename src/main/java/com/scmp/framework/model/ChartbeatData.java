package com.scmp.framework.model;

/**
 * Refer to Chartbeat parameters: https://docs.chartbeat.com/cbp/tracking/standard-websites/qa-web-integration
 */
public class ChartbeatData extends AbstractTrackingData {

	public ChartbeatData(String original) {
		super(original);
	}

	public String getHost() {
		return this.getValue(ChartbeatParameter.Host);
	}

	public String getPath() {
		return this.getValue(ChartbeatParameter.Path);
	}

	public String getSections() {
		return this.getValue(ChartbeatParameter.Sections);
	}

	public String getAuthors() {
		return this.getValue(ChartbeatParameter.Authors);
	}

	public String getValue(ChartbeatParameter parameter) {
		return this.getVariables().get(parameter.toString());
	}

	public String getValue(String parameter) {
		return this.getVariables().get(parameter);
	}
}
