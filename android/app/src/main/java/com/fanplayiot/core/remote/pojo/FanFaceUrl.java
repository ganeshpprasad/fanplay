package com.fanplayiot.core.remote.pojo;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FanFaceUrl extends BaseData<FanFaceUrl> {
    private String url;

    public String getUrl() {
        return url;
    }

    @Override
    protected JSONObject getJSONObject() {
        return null;
    }

    @Override
    public @Nullable
    FanFaceUrl fromJSONObject(JSONObject jsonObject) throws JSONException {
        JSONArray array = jsonObject.getJSONObject("response").getJSONArray("result");
        if (array.length() > 0) {
            url = array.getJSONObject(0).getString("profileimage");
            return this;
        }
        return null;
    }
}
