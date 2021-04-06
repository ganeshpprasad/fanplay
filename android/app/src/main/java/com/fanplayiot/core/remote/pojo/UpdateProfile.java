package com.fanplayiot.core.remote.pojo;

import android.net.Uri;
import android.util.Log;

import com.fanplayiot.core.db.local.entity.User;
import com.fanplayiot.core.utils.Constant;

public class UpdateProfile {
    private static final String TAG = "UserProfile";
    private User user;
    private String imagePath;
    private int affiliationId;

    public void setUser(User user) {
        this.user = user;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public void setAffiliationId(int affiliationId) {
        this.affiliationId = affiliationId;
    }

    public String getURLWithParams() {
        Uri.Builder builtUri = Uri.parse(Constant.BASEURL + Constant.POST_USERPROFILE)
                .buildUpon();
        try {
            if (user == null) {
                Log.d(TAG, "UpdateProfile: User is null");
                return null;
            }
            if (user.getSid() == null) {
                Log.d(TAG, "UpdateProfile: User Sid is not set");
                return null;
            }
            if (user.getSid() == 0L) {
                Log.d(TAG, "UpdateProfile: User Sid is not set");
                return null;
            }
            builtUri.appendQueryParameter("sid", String.valueOf(user.getSid()));
            if (user.getTeamPref() > 0) {
                builtUri.appendQueryParameter("teamid", String.valueOf(user.getTeamPref()));
            }
            if (user.getGender() != null && !user.getGender().isEmpty()) {
                builtUri.appendQueryParameter("gender", user.getGender());
            } else {
                builtUri.appendQueryParameter("gender", "Male");
            }
            if (user.getMobile() != null && !user.getMobile().isEmpty()) {
                builtUri.appendQueryParameter("mobile", user.getMobile());
            }
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                builtUri.appendQueryParameter("email", user.getEmail());
            }
            if (user.getDob() != null && !user.getDob().isEmpty()) {
                builtUri.appendQueryParameter("dob", user.getDob());
            }
            if (user.getAge() > 0) {
                builtUri.appendQueryParameter("age", String.valueOf(user.getAge()));
            }
            if (user.getCity() != null && !user.getCity().isEmpty()) {
                builtUri.appendQueryParameter("city", user.getCity());
            }
            if (user.getHeight() != null && !user.getHeight().isEmpty()) {
                builtUri.appendQueryParameter("height", user.getHeight());
            }
            if (user.getHeightMeasure() != null && !user.getHeightMeasure().isEmpty()) {
                builtUri.appendQueryParameter("heightmeasure", user.getHeightMeasure());
            }
            if (user.getWeight() != null && !user.getWeight().isEmpty()) {
                builtUri.appendQueryParameter("weight", user.getWeight());
            }
            if (user.getWeightMeasure() != null && !user.getWeightMeasure().isEmpty()) {
                builtUri.appendQueryParameter("weightmeasure", user.getWeightMeasure());
            }
            if (user.getDeviceId() != null && !user.getDeviceId().isEmpty()) {
                builtUri.appendQueryParameter("deviceId", user.getDeviceId());
            }
            if (user.getPhoneDeviceInfo() != null && !user.getPhoneDeviceInfo().isEmpty()) {
                builtUri.appendQueryParameter("phonedeviceinfo", user.getPhoneDeviceInfo());
            }
            //affiliationid
            if (affiliationId > 0) {
                builtUri.appendQueryParameter("affiliationid", String.valueOf(affiliationId));
            }
        } catch (Exception e) {
            Log.d(TAG, "hash map error", e);
            return null;
        }
        return builtUri.build().toString();
    }

}