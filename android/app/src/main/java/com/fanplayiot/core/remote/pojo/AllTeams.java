package com.fanplayiot.core.remote.pojo;

public class AllTeams extends BaseArray {

    public int getSize() {
        return super.getSize();
    }

    public Long[] getIds() {
        return super.getIds();
    }

    public String[] getTeamNames() {
        return super.getNames();
    }

    @Override
    public String getIdsJSONName() {
        return "teamid";
    }

    @Override
    public String getNamesJSONName() {
        return "teamname";
    }
}
