package com.fanplayiot.core.remote.pojo;

public class AllHealthIssues extends BaseList {
    @Override
    public String getJSONArrayName() {
        return "healthissues";
    }

    @Override
    public String getIdObjectName() {
        return "id";
    }

    @Override
    public String getNameObjectName() {
        return "name";
    }

    @Override
    public String getLogoObjectName() {
        return "logo";
    }
}
