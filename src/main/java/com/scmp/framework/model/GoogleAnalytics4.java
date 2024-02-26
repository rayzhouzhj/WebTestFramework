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

    public String getDocumentLocation() {
        return this.getValue(GoogleAnalytics4Parameter.DOCUMENT_LOCATION);
    }

    public String getValue(GoogleAnalytics4Parameter parameter) {
        return this.getVariables().get(parameter.toString());
    }

    public String getValue(String parameter) {
        return this.getVariables().get(parameter);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }

        if (!(o instanceof GoogleAnalytics4)){
            return false;
        }

        GoogleAnalytics4 that = (GoogleAnalytics4) o;

        boolean isEqual = this.getVariables().entrySet().stream().allMatch(entry -> {
            String key = entry.getKey();
            String value = entry.getValue();
            return value.equals(that.getVariables().get(key));
        });

        return isEqual;
    }
}
