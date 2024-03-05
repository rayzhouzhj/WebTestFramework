package com.scmp.framework.model;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class GoogleAnalytics4 extends AbstractTrackingData {

    Map<String, String> parameters;

    public GoogleAnalytics4(String original, String query) {
        super(original);
        this.parameters = new HashMap<>();

        String[] parameters = query.split("&");

        Arrays.stream(parameters).forEach(parameter -> {
            String[] keyValue = parameter.split("=");
            this.parameters.put(keyValue[0], keyValue[1]);
        });
    }

    public GoogleAnalytics4(String original) {
        super(original);
    }

    public String getEventName() {
        return this.getValue(GoogleAnalytics4Parameter.EVENT_NAME);
    }

    public String getEventData(String key) {

        if(parameters != null){
            return parameters.get(key) != null? parameters.get(key): null;
        }else{
            return this.getValue(GoogleAnalytics4Parameter.EVENT_DATA + "." + key);
        }
    }

    public String getDocumentLocation() {
        return this.getValue(GoogleAnalytics4Parameter.DOCUMENT_LOCATION);
    }

    public String getValue(GoogleAnalytics4Parameter parameter) {
        if(parameters != null){
            return parameters.get(parameter.toString());
        }else{
            return this.getVariables().get(parameter.toString());
        }
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

        boolean isQueryEqual = this.getVariables().entrySet().stream().allMatch(entry -> {
            String key = entry.getKey();
            String value = entry.getValue();
            return value.equals(that.getVariables().get(key));
        });

        AtomicBoolean isParametersEqual = new AtomicBoolean(true);

        if(this.parameters!= null && that.parameters != null){

            if(that.parameters.size() == this.parameters.size()){
                this.parameters.keySet().forEach(key -> {
                    if(!that.parameters.containsKey(key) && !that.parameters.get(key).equals(this.parameters.get(key))){
                        isParametersEqual.set(false);
                    }
                });
            }
        }else if(this.parameters == null && that.parameters == null){
            isParametersEqual.set(true);
        }else{
            isParametersEqual.set(false);
        }

        return isQueryEqual && isParametersEqual.get();
    }
}
