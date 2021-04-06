package com.fanplayiot.core.remote.pojo;

import org.json.JSONException;
import org.json.JSONObject;

public class DisplayTotalUser extends BaseData<DisplayTotalUser> {
    private static final String TAG = "DisplayTotalUser";
    public static final String DISPLAYTOTALUSER = "DisplayTotalUser";
    public static final String TOTALUSER = "TotalUser";
    private int totalUser;

    public DisplayTotalUser() {
    }

    public DisplayTotalUser(int totalUser) {
        this.totalUser = totalUser;
    }

    private DisplayTotalUser(JSONObject object) throws Exception {
        try {
            //JSONObject TotalUser = object.getJSONArray(DISPLAYTOTALUSER).getJSONObject(0);
            //this.totalUser = TotalUser.getInt(TOTALUSER);
            fromJSONObject(object);
        } catch (JSONException e) {
            // log error
            throw e;
        }
    }

    public int getTotalUser() {
        return totalUser;
    }

    @Override
    public JSONObject getJSONObject() {
        return null;
    }

    @Override
    public DisplayTotalUser fromJSONObject(JSONObject jsonObject) throws JSONException {
        JSONObject TotalUser = jsonObject.getJSONArray(DISPLAYTOTALUSER).getJSONObject(0);
        this.totalUser = TotalUser.getInt(TOTALUSER);
        return this;
    }

}
