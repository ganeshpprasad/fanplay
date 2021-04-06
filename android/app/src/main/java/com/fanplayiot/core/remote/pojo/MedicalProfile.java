package com.fanplayiot.core.remote.pojo;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fanplayiot.core.db.local.entity.Medical;
import com.fanplayiot.core.db.local.entity.Profile;
import com.fanplayiot.core.db.local.entity.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

public class MedicalProfile extends BaseData<MedicalProfile>{
    private static final String TAG = "MedicalProfile";
    private Profile profile;

    public void setProfile(@NonNull Profile profile) {
        this.profile = profile;
    }

    @Override
    public @Nullable JSONObject getJSONObject() {
        if (profile != null) {
            User user = profile.getUser();
            Medical medical = profile.getMedical();
            JSONObject object = new JSONObject();
            try {
                if (user == null) {
                    Log.d(TAG, "MedicalProfile: User is null");
                    return null;
                }
                if (user.getSid() == 0L) {
                    Log.d(TAG, "MedicalProfile: User Sid is not set");
                    return null;
                }
                object.put("sid", user.getSid());
                Set<String> health = medical.getHealthIssues();
                JSONArray hiArray = new JSONArray();
                if (health != null && health.size() > 0) {
                    for(String healthStr : health) {
                        JSONObject hiObj = new JSONObject();
                        try {
                            int id = Integer.parseInt(healthStr);
                            if (id > 0) {
                                hiObj.put("healthissueid", id);
                                hiArray.put(hiObj);
                            }
                        } catch (NumberFormatException ignore) {

                        }
                    }
                    object.put("healthissuedetails", hiArray);
                }

                Set<String> habits = medical.getHabits();
                JSONArray haArray = new JSONArray();
                if (habits != null && habits.size() > 0) {
                    for(String habitStr : habits) {
                        JSONObject haObj = new JSONObject();
                        try {
                            int id = Integer.parseInt(habitStr);
                            if (id > 0) {
                                haObj.put("habitid", id);
                                haArray.put(haObj);
                            }
                        } catch (NumberFormatException ignore) {

                        }
                    }
                    object.put("habitdetails", haArray);
                }
                object.put("bloodsugar", medical.getBloodSugarStr());
                object.put("bloodpressure", medical.getBloodPressure());
                object.put("heartrate", medical.getHeartRate());
                return object;
            } catch (JSONException je) {
                Log.d(TAG, "json error", je);
            }
        } else {
            Log.d(TAG, "Profile is null");
        }
        return null;
    }

    @Override
    protected MedicalProfile fromJSONObject(JSONObject jsonObject) {
        return null;
    }
}
