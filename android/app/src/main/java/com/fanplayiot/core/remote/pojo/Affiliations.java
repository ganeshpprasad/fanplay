package com.fanplayiot.core.remote.pojo;

public class Affiliations extends BaseArray {

    public int getSize() {
        return super.getSize();
    }

    public Long[] getIds() {
        return super.getIds();
    }

    public String[] getAffNames() {
        return super.getNames();
    }

    @Override
    public String getIdsJSONName() {
        return "affiliationid";
    }

    @Override
    public String getNamesJSONName() {
        return "affiliationname";
    }
}
