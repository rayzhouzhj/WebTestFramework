package com.scmp.framework.model;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Refer to GA parameters: https://cheatography.com/dmpg-tom/cheat-sheets/google-universal-analytics-url-collect-parameters/
 */
public class GoogleAnalystics {
    private String originalUrl = "";
    private Map<String, String> variables = new HashMap<>();

    public GoogleAnalystics(String original) {
        this.originalUrl = original;
        this.parse(this.originalUrl);
    }

    public GoogleAnalystics parse(String url) {
        try {
            this.variables = this.splitQuery(new URL(url));
        } catch (Exception e) {

        }

        return this;
    }

    public String getOriginalURL() {
        return this.originalUrl;
    }

    public boolean isEventType() {
        return "event".equalsIgnoreCase(getTpyeOfTracking());
    }

    public boolean isPageViewType() {
        return "pageview".equalsIgnoreCase(getTpyeOfTracking());
    }

    public String getTpyeOfTracking() {
        return this.getValue(GoogleAnalysticsParameter.Type_Of_Tracking);
    }

    public String getEventAction() {
        return this.getValue(GoogleAnalysticsParameter.Event_Action);
    }

    public String getEventCategory() {
        return this.getValue(GoogleAnalysticsParameter.Event_Category);
    }

    public String getEventLabel() {
        return this.getValue(GoogleAnalysticsParameter.Event_Label);
    }

    public String getEventValue() {
        return this.getValue(GoogleAnalysticsParameter.Event_Value);
    }

    public String getDocumentTitle() {
        return this.getValue(GoogleAnalysticsParameter.Document_Title);
    }

    public String getDocumentPath() {
        return this.getValue(GoogleAnalysticsParameter.Document_Path);
    }

    public String getDocumentHost() {
        return this.getValue(GoogleAnalysticsParameter.Document_Host);
    }

    public String getDocumentLocation() {
        return this.getValue(GoogleAnalysticsParameter.Document_Location);
    }

    public String getDocumentEncodingType() {
        return this.getValue(GoogleAnalysticsParameter.Document_Encoding_Type);
    }

    public String getDocumentClientID() {
        return this.getValue(GoogleAnalysticsParameter.Client_ID);
    }

    public String getDocumentCampaignName() {
        return this.getValue(GoogleAnalysticsParameter.Campaign_Name);
    }

    public String getDocumentCampaignSource() {
        return this.getValue(GoogleAnalysticsParameter.Campaign_Source);
    }

    public String getDocumentCampaignMedium() {
        return this.getValue(GoogleAnalysticsParameter.Campaign_Medium);
    }

    public String getDocumentCampaignKeyword() {
        return this.getValue(GoogleAnalysticsParameter.Campaign_Keyword);
    }

    public String getDocumentCampaignContent() {
        return this.getValue(GoogleAnalysticsParameter.Campaign_Content);
    }

    public String getDocumentCampaignID() {
        return this.getValue(GoogleAnalysticsParameter.Campaign_Id);
    }

    public String getGoogleAdwordsID() {
        return this.getValue(GoogleAnalysticsParameter.Google_Adwords_ID);
    }

    public String getGoogleDisplayAdsID() {
        return this.getValue(GoogleAnalysticsParameter.Google_Display_Ads_ID);
    }

    public String getTrackingID() {
        return this.getValue(GoogleAnalysticsParameter.Tracking_ID);
    }

    public String getScreenResolution() {
        return this.getValue(GoogleAnalysticsParameter.Screen_Resolution);
    }

    public String getScreenDepth() {
        return this.getValue(GoogleAnalysticsParameter.Screen_Depth);
    }

    public String getUserLanguageCode() {
        return this.getValue(GoogleAnalysticsParameter.User_Language_Code);
    }

    public String getCustomDimension(int index) {
        return this.getValue("cd" + index);
    }

    public String getValue(GoogleAnalysticsParameter parameter) {
        return this.variables.get(parameter.toString());
    }

    public String getValue(String parameter) {
        return this.variables.get(parameter);
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
