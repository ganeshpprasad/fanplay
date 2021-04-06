package com.fanplayiot.core.remote.pojo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class BaseArray extends BaseData<BaseArray>{
    private int size;
    private Long[] ids;
    private String[] names;

    public int getSize() {
        return size;
    }

    public Long[] getIds() {
        return ids;
    }

    public String[] getNames() {
        return names;
    }

    @Override
    protected JSONObject getJSONObject() {
        return null;
    }

    @Override
    public BaseArray fromJSONObject(JSONObject jsonObject) throws JSONException {
        JSONArray array = jsonObject.getJSONObject("response").getJSONArray("result");
        if (array.length() > 0) {
            size = array.length();
            ids = new Long[size];
            names = new String[size];
            for (int i = 0; i < size; i++) {
                JSONObject item = array.getJSONObject(i);
                ids[i] = (long) item.getInt(getIdsJSONName());
                names[i] = item.getString(getNamesJSONName());
            }
        }
        return this;
    }

    public abstract String getIdsJSONName();
    public abstract String getNamesJSONName();
}
