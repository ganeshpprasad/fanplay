package com.fanplayiot.core.remote.pojo;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fanplayiot.core.db.local.entity.Goal;
import com.fanplayiot.core.db.local.entity.PhysicalActivity;
import com.fanplayiot.core.db.local.entity.Profile;
import com.fanplayiot.core.db.local.entity.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class PhysicalGoal extends BaseData<PhysicalGoal>{
    private static final String TAG = "PhysicalGoal";
    private Profile profile;

    public void setProfile(@NonNull Profile profile) {
        this.profile = profile;
    }

    @Override
    public @Nullable
    JSONObject getJSONObject() {
        if (profile != null) {
            User user = profile.getUser();
            Goal goal= profile.getGoal();
            JSONObject object = new JSONObject();
            try {
                if (user == null) {
                    Log.d(TAG, "PhysicalGoal: User is null");
                    return null;
                }
                if (user.getSid() == 0L) {
                    Log.d(TAG, "PhysicalGoal: User Sid is not set");
                    return null;
                }
                object.put("sid", user.getSid());
                List<PhysicalActivity> paSet = profile.getPhysicalActivities();
                if (paSet.size() > 0) {
                    JSONArray haArray = new JSONArray();
                    for(PhysicalActivity pa : paSet) {
                        JSONObject haObj = new JSONObject();
                        try {
                            haObj.put("activityid", pa.getPid());
                            haObj.put("perdayhour", pa.getPerDayHour());
                            haObj.put("perdayminutes", pa.getPerDayMin());
                            haObj.put("perweekhour", pa.getPerWeekHour());
                            haObj.put("perweekminutes", pa.getPerWeekMin());
                            haArray.put(haObj);
                        } catch (NumberFormatException ignore) {

                        }
                    }
                    object.put("physicalactivities", haArray);
                }
                object.put("steps", goal.getSteps());
                object.put("calories", goal.getCalories());
                object.put("distance", goal.getDistance());
                int hoursInt = (int) goal.getSleepHours();
                if (hoursInt > 0 && hoursInt <= 24) {
                    object.put("sleeping", hoursInt);
                } else {
                    object.put("sleeping", 0);
                }
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
    protected PhysicalGoal fromJSONObject(JSONObject jsonObject) {
        return null;
    }
}
