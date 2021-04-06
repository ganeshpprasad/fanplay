package com.fanplayiot.core.db.local.dao;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import static com.fanplayiot.core.db.ConstantKt.*;


public class ScoreHelper {

    public static final float MHR_ZONE_50 = 0.5f, MHR_ZONE_60 = 0.6f, MHR_ZONE_70 = 0.7f, MHR_ZONE_80 = 0.8f;
    public static final int DEFAULT_AGE = 32;

    public static float hrAgeToPoints(int age, float hr) {
        float maxHr = (float) (220 - age);
        return Math.round(hr / (maxHr / 10));
    }

    /**
     * Get Max Heart Rate Zone
     * @param sp Shared preferences obj
     * @param age age
     * @param hr current heart rate
     * @return hr zone
     */
    public static int maxHrZone(@NonNull SharedPreferences sp, int age, int hr) {
        float mHr50 = sp.getFloat(HR_ZONE_50_KEY, (220 - age) * MHR_ZONE_50);
        float mHr60 = sp.getFloat(HR_ZONE_60_KEY, (220 - age) * MHR_ZONE_60);
        float mHr70 = sp.getFloat(HR_ZONE_70_KEY, (220 - age) * MHR_ZONE_70);
        float mHr80 = sp.getFloat(HR_ZONE_80_KEY, (220 - age) * MHR_ZONE_80);
        if (hr < mHr50) return 1;
        else if (mHr50 >= hr && hr < mHr60) return 2;
        else if (mHr60 >= hr && hr < mHr70) return 3;
        else if (mHr70 >= hr && hr < mHr80) return 4;
        else return 5;
    }

    /**
     * Get values for given heart rate based on criteria
     * @param hr heart rate in float
     * @return value based on mapping
     */
    public static float hrToPoints(float hr) {
        if (hr < 50) return 2.5f;
        else if (hr >= 50 && hr < 70) return 4.2f;
        else if (hr >= 70 && hr < 90) return 6.3f;
        else if (hr >= 90 && hr < 110) return 6.8f;
        return  8f;
    }

    /**
     * Find delta of two numbers
     * @param one first number (positive number)
     * @param two second number (positive number)
     * @return delta value
     */
    public static int deltaOf(int one, int two) {
        if (two == 0) return one;
        if (two == one) return 0;
        if (two > one) return two - one;
        return one - two;
    }
    /**
     * Convert any counts delta values to points based on look up table
     * @param count counts like tap count or wave count
     * @return points in float
     */
    public static float deltaToPoints(int count) {
        if (count <= 4) {
            // value 0 to 4
            return 0.0f;
        } else if (count < 50) {
            return 0.25f;
        } else if (count < 100) {
            return 0.3f;
        } else if (count < 250) {
            return 0.4f;
        } else if (count < 500) {
            return 0.5f;
        } else if (count < 750) {
            return 0.6f;
        } else if (count < 1000) {
            return 0.7f;
        } else if (count < 2000) {
            return 0.8f;
        } else if (count < 3000) {
            return 0.9f;
        } else if (count < 4000) {
            return 1f;
        } else {
            // value > 4000
            return 1f;
        }
    }

    @SuppressWarnings("DuplicateBranchesInSwitch")
    private static float getZoneMultiplier(int hrZone) {
        switch (hrZone) {
            case 1: return 0.75f;
            case 2: return 1f;
            case 3: return 1.25f;
            case 4: return 1.5f;
            default: return 1f;
        }
    }

    public static long calcNewPoints(int whistleRedeemed, long newPoints, int hrZone ) {
        return (long) (newPoints + (newPoints * getZoneMultiplier(hrZone)) + (whistleRedeemed * 25));
    }
}
