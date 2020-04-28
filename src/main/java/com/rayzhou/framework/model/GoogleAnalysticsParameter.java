package com.rayzhou.framework.model;

public enum GoogleAnalysticsParameter {
    Type_Of_Tracking("t"),
    Event_Action("ea"),
    Event_Category("ec"),
    Event_Value("ev"),
    Event_Label("el"),
    Document_Title("dt"),
    Document_Path("dp"),
    Document_Host("dh"),
    Document_Location("dl"),
    Document_Encoding_Type("de"),
    Client_ID("cid"),
    Campaign_Name("cn"),
    Campaign_Source("cs"),
    Campaign_Medium("cm"),
    Campaign_Keyword("ck"),
    Campaign_Content("cc"),
    Campaign_Id("ci"),
    Google_Adwords_ID("glcid"),
    Google_Display_Ads_ID("dclid"),
    Tracking_ID("tid"),
    Screen_Resolution("sr"),
    Screen_Depth("sd"),
    User_Language_Code("ul");

    private String parameter;
    GoogleAnalysticsParameter(String parameter) {
        this.parameter = parameter;
    }

    @Override
    public String toString() {
        return this.parameter;
    }
}

