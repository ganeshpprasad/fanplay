package com.fanplayiot.core.remote.pojo;

import android.util.Log;

import androidx.annotation.NonNull;

import com.fanplayiot.core.db.local.entity.Goal;
import com.fanplayiot.core.db.local.entity.Profile;
import com.fanplayiot.core.db.local.entity.User;
import com.fanplayiot.core.db.local.entity.UserPref;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

public class SportsTeam extends BaseData<SportsTeam> {
    private static final String TAG = "SportsTeam";
    private Profile profile;

    public void setProfile(@NonNull Profile profile) {
        this.profile = profile;
    }

    @Override
    public JSONObject getJSONObject() {
        if (profile != null) {
            User user = profile.getUser();
            UserPref pref = profile.getUserPref();

            JSONObject object = new JSONObject();
            try {
                if (user == null) {
                    Log.d(TAG, "SportsTeam: User is null");
                    return null;
                }
                if (user.getSid() == 0L) {
                    Log.d(TAG, "SportsTeam: User Sid is not set");
                    return null;
                }
                object.put("sid", user.getSid());
                Set<String> sports = pref.getSportsIds();
                if (sports != null && sports.size() > 0) {
                    JSONArray spArray = new JSONArray();
                    for (String str : sports) {
                        JSONObject spObj = new JSONObject();
                        try {
                            int id = Integer.parseInt(str);
                            spObj.put("sportid", id);
                            spArray.put(spObj);
                        } catch (NumberFormatException ignore) {

                        }
                    }
                    object.put("favouritesports", spArray);
                }
                if (pref.getAffiliationId() > 0) {
                    object.put("affiliationid", pref.getAffiliationId());
                }
                object.put("teamid", pref.getTeamPrefId());
                object.put("doyoufollowanyfitnessregimen", pref.isFollowFitness());
                object.put("interestedincustomizedfitnessregimen", pref.isCustomFitness());
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
    protected SportsTeam fromJSONObject(JSONObject jsonObject) throws JSONException {
        return null;
    }

    /*
    {
  "sid": 0,
  "favouritesports": [
    {
      "sportid": 0
    }
  ],
  "affiliationid": 0,
  "teamid": 0,
  "doyoufollowanyfitnessregimen": true,
  "interestedincustomizedfitnessregimen": true
}
     */
}
