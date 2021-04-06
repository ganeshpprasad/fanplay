package com.fanplayiot.core.remote.pojo;

import org.json.JSONException;
import org.json.JSONObject;

public class ApiResponse extends BaseData<ApiResponse> {
    public static final String RESPONSE = "RESPONSE";
    String reponse;

    public String getReponse() {
        return reponse;
    }

    public void setReponse(String reponse) {
        this.reponse = reponse;
    }

    @Override
    protected JSONObject getJSONObject() {
        try {
            return new JSONObject(reponse);
        } catch (JSONException e) {
            return null;
        }
    }

    @Override
    protected ApiResponse fromJSONObject(JSONObject jsonObject) throws JSONException {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setReponse(jsonObject.getString("NAME"));
        return apiResponse;
    }
}
