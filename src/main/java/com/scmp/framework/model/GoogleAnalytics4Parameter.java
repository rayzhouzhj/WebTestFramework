package com.scmp.framework.model;

public enum GoogleAnalytics4Parameter {

    EVENT_NAME("en"),
    EVENT_DATA("ep");

    private String parameter;

    GoogleAnalytics4Parameter(String parameter) {
        this.parameter = parameter;
    }

    @Override
    public String toString() {
        return this.parameter;
    }
}
