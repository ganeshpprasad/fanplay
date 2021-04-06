package com.fanplayiot.core.remote.pojo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FanEmote extends BaseData<FanEmote> {
    private double avgFanEmote;
    private int avgHeartRate;
    private String[] annoucements;

    public double getAvgFanEmote() {
        return avgFanEmote;
    }

    public int getAvgHeartRate() {
        return avgHeartRate;
    }

    public String[] getAnnoucements() {
        return annoucements;
    }

    @Override
    protected JSONObject getJSONObject() {
        return null;
    }

    @Override
    protected FanEmote fromJSONObject(JSONObject jsonObject) throws JSONException {
        JSONArray jsonArray = jsonObject.getJSONObject("response")
                .getJSONArray("fanemote");
        if (jsonArray.length() > 0 ) {
            JSONObject fanObj = jsonArray.getJSONObject(0);
            avgFanEmote = fanObj.getDouble("averagefanemote");
            avgHeartRate = fanObj.getInt("averageheartrate");
            JSONArray annouArray = fanObj.getJSONArray("annoucements");
            if (annouArray.length() > 0) {
                String[] temp = new String[annouArray.length()];
                for (int i = 0; i < annouArray.length(); i++) {
                    JSONObject tempObj = annouArray.getJSONObject(i);
                    temp[i] = tempObj.getString("annoucements");
                }
                annoucements = temp;
            }
        }
        return this;
    }
}
