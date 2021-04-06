package com.fanplayiot.core.remote.pojo;

import android.util.Log;

import com.fanplayiot.core.db.local.entity.User;

import org.json.JSONException;
import org.json.JSONObject;

public class SignInUser extends BaseData<SignInUser> {
    private static final String TAG = "SignInUser";
    /*
    {
  "roleid": 0,
  "displayname": "string",
  "latitiude": 0,
  "longitude": 0
    }
     */
    private User user;
    private String displayName;

    public void setUser(User user) {
        this.user = user;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public JSONObject getJSONObject() {
        JSONObject object = new JSONObject();
        try {
            // For mobile users roleId is 3
            object.put("roleId", 3);
            String name = (user != null) ? user.getProfileName() : displayName;
            object.put("displayname", (name != null && !name.trim().isEmpty()) ? name : "");
            object.put("latitiude", (user != null) ? user.getLatitude() : 0);
            object.put("longitude", (user != null) ? user.getLongitude() : 0);

        } catch (JSONException je) {
            Log.d(TAG, "json error", je);
        }
        return object;
    }

    @Override
    protected SignInUser fromJSONObject(JSONObject jsonObject) throws JSONException {
        return null;
    }
}
