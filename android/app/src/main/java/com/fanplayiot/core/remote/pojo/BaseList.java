package com.fanplayiot.core.remote.pojo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseList extends BaseData<BaseList>{
    private List<NameLogo> baseItemList;
    private String jsonString;

    public List<NameLogo> getBaseItemList() {
        return baseItemList;
    }

    public void setJsonString(@NonNull String jsonString) {
        this.jsonString = jsonString;
    }

    public @Nullable String getJsonString() {
        return jsonString;
    }

    @Override
    protected JSONObject getJSONObject() {
        return null;
    }

    @Override
    public BaseList fromJSONObject(JSONObject jsonObject) throws JSONException {
        JSONArray array = jsonObject.optJSONArray(getJSONArrayName());
        if (array != null && array.length() > 0) {
            int size = array.length();
            baseItemList = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                JSONObject item = array.getJSONObject(i);
                NameLogo baseItem = new NameLogo(
                    item.getInt(getIdObjectName()),
                    item.getString(getNameObjectName()),
                    item.getString(getLogoObjectName())
                );
                baseItemList.add(baseItem);
            }
        }
        jsonString = jsonObject.toString();
        return this;
    }

    public abstract String getJSONArrayName();
    public abstract String getIdObjectName();
    public abstract String getNameObjectName();
    public abstract String getLogoObjectName();

}
