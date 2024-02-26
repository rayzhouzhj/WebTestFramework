package com.scmp.framework.model;

public class GoogleAnalytics4 extends AbstractTrackingData {

    public GoogleAnalytics4(String original) {
        super(original);
    }

    public String getEventName() {
        return this.getValue(GoogleAnalytics4Parameter.EVENT_NAME);
    }

    public String getEventData(String key) {
        return this.getValue(GoogleAnalytics4Parameter.EVENT_DATA + "." + key);
    }

    public String getValue(GoogleAnalytics4Parameter parameter) {
        return this.getVariables().get(parameter.toString());
    }

    public String getValue(String parameter) {
        return this.getVariables().get(parameter);
    }
}
