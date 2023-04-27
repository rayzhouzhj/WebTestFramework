package com.scmp.framework.model;

/**
 * Refer to GA parameters: https://cheatography.com/dmpg-tom/cheat-sheets/google-universal-analytics-url-collect-parameters/
 */
public class GoogleAnalytics extends AbstractTrackingData {

    public GoogleAnalytics(String original) {
        super(original);
    }

    public boolean isEventType() {
        return "event".equalsIgnoreCase(getTpyeOfTracking());
    }

    public boolean isPageViewType() {
        return "pageview".equalsIgnoreCase(getTpyeOfTracking());
    }

    public String getTpyeOfTracking() {
        return this.getValue(GoogleAnalyticsParameter.Type_Of_Tracking);
    }

    public String getEventAction() {
        return this.getValue(GoogleAnalyticsParameter.Event_Action);
    }

    public String getEventCategory() {
        return this.getValue(GoogleAnalyticsParameter.Event_Category);
    }

    public String getEventLabel() {
        return this.getValue(GoogleAnalyticsParameter.Event_Label);
    }

    public String getEventValue() {
        return this.getValue(GoogleAnalyticsParameter.Event_Value);
    }

    public String getDocumentTitle() {
        return this.getValue(GoogleAnalyticsParameter.Document_Title);
    }

    public String getDocumentPath() {
        return this.getValue(GoogleAnalyticsParameter.Document_Path);
    }

    public String getDocumentHost() {
        return this.getValue(GoogleAnalyticsParameter.Document_Host);
    }

    public String getDocumentLocation() {
        return this.getValue(GoogleAnalyticsParameter.Document_Location);
    }

    public String getDocumentEncodingType() {
        return this.getValue(GoogleAnalyticsParameter.Document_Encoding_Type);
    }

    public String getDocumentClientID() {
        return this.getValue(GoogleAnalyticsParameter.Client_ID);
    }

    public String getDocumentCampaignName() {
        return this.getValue(GoogleAnalyticsParameter.Campaign_Name);
    }

    public String getDocumentCampaignSource() {
        return this.getValue(GoogleAnalyticsParameter.Campaign_Source);
    }

    public String getDocumentCampaignMedium() {
        return this.getValue(GoogleAnalyticsParameter.Campaign_Medium);
    }

    public String getDocumentCampaignKeyword() {
        return this.getValue(GoogleAnalyticsParameter.Campaign_Keyword);
    }

    public String getDocumentCampaignContent() {
        return this.getValue(GoogleAnalyticsParameter.Campaign_Content);
    }

    public String getDocumentCampaignID() {
        return this.getValue(GoogleAnalyticsParameter.Campaign_Id);
    }

    public String getGoogleAdwordsID() {
        return this.getValue(GoogleAnalyticsParameter.Google_Adwords_ID);
    }

    public String getGoogleDisplayAdsID() {
        return this.getValue(GoogleAnalyticsParameter.Google_Display_Ads_ID);
    }

    public String getTrackingID() {
        return this.getValue(GoogleAnalyticsParameter.Tracking_ID);
    }

    public String getScreenResolution() {
        return this.getValue(GoogleAnalyticsParameter.Screen_Resolution);
    }

    public String getScreenDepth() {
        return this.getValue(GoogleAnalyticsParameter.Screen_Depth);
    }

    public String getUserLanguageCode() {
        return this.getValue(GoogleAnalyticsParameter.User_Language_Code);
    }

    public String getCustomDimension(int index) {
        return this.getValue("cd" + index);
    }

    public String getValue(GoogleAnalyticsParameter parameter) {
        return this.getVariables().get(parameter.toString());
    }

    public String getValue(String parameter) {
        return this.getVariables().get(parameter);
    }
}
