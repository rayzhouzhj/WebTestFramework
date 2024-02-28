package com.scmp.framework.model;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class GoogleAnalytics4 extends AbstractTrackingData {

    List<String> parameters;

    public GoogleAnalytics4(String original, List<String> parameters) {
        super(original);
        this.parameters = parameters;
    }

    public GoogleAnalytics4(String original) {
        super(original);
    }

    public String getEventName() {
        return this.getValue(GoogleAnalytics4Parameter.EVENT_NAME);
    }

    public String getEventData(String key) {

        if(parameters != null){
            Optional<String> keyValueOptional = parameters.stream().filter(parameter -> parameter.contains(GoogleAnalytics4Parameter.EVENT_DATA + "." + key)).findFirst();

            if(keyValueOptional.isPresent()){
                return keyValueOptional.get().split("=")[1];
            }else{
                return null;
            }
        }else{
            return this.getValue(GoogleAnalytics4Parameter.EVENT_DATA + "." + key);
        }
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

        boolean isQueryEqual = this.getVariables().entrySet().stream().allMatch(entry -> {
            String key = entry.getKey();
            String value = entry.getValue();
            return value.equals(that.getVariables().get(key));
        });

        AtomicBoolean isParametersEqual = new AtomicBoolean(true);

        if(this.parameters!= null && that.parameters != null){

            if(that.parameters.size() == this.parameters.size()){
                this.parameters.forEach(parameter -> {
                    if(!that.parameters.contains(parameter)){
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
