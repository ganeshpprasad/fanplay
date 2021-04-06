package com.fanplayiot.core.remote.pojo;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fanplayiot.core.db.local.entity.FitnessBP;
import com.fanplayiot.core.db.local.entity.FitnessHR;
import com.fanplayiot.core.db.local.entity.FitnessKt;
import com.fanplayiot.core.db.local.entity.FitnessSCD;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.fanplayiot.core.db.local.entity.FitnessKt.DEVICE_BAND;
import static com.fanplayiot.core.db.local.entity.FitnessKt.DISTANCE_UNIT_KM;
import static com.fanplayiot.core.db.local.entity.FitnessKt.GOOGLE_FIT;
import static com.fanplayiot.core.db.local.entity.FitnessKt.PHONE;
import static com.fanplayiot.core.remote.pojo.BaseDataKt.getTimeStamp;

public class FanFitness extends BaseData<FanFitness> {
    private static final String TAG = "FanFitness";
    private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static final String DATE_PATTERN1 = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    private static final String DATE_PATTERN2 = "yyyy-MM-dd'T'HH:mm:ss";
    private long sid;
    private List<FitnessSCD> scdList;
    private List<FitnessHR> hrList;
    private List<FitnessBP> bpList;
    private int pageId;
    private int deviceType = 3;

    public void setSid(long sid) {
        this.sid = sid;
    }

    public void setScdList(@NonNull List<FitnessSCD> scdList) {
        this.scdList = scdList;
    }

    public void setHrList(@NonNull List<FitnessHR> hrList) {
        this.hrList = hrList;
    }

    public void setBpList(@NonNull List<FitnessBP> bpList) {
        this.bpList = bpList;
    }

    public void setDeviceType(int localType) {
        if (localType == FitnessKt.DEVICE_BAND) {
            this.deviceType = 3;
        } else if (localType == FitnessKt.PHONE) {
            this.deviceType = 1;
        } else {
            this.deviceType = localType;
        }
    }

    private int getLocalType(int type) {
        if (type == 3) return FitnessKt.DEVICE_BAND;
        else if (type == 1) return FitnessKt.PHONE;
        else if (type == 2) return FitnessKt.GOOGLE_FIT;
        return -1;
    }

    @Nullable
    public List<FitnessSCD> getScdList() {
        return scdList;
    }

    @Nullable
    public List<FitnessHR> getHrList() {
        return hrList;
    }

    @Nullable
    public List<FitnessBP> getBpList() {
        return bpList;
    }

    public int getPageId() {
        return pageId;
    }

    public void setPageId(int pageId) {
        this.pageId = pageId;
    }

    @Override
    public JSONObject getJSONObject() {
        JSONObject object = new JSONObject();
        // "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
        // "yyyy-MM-dd'T'HH:mm:ss'Z'"
        SimpleDateFormat format = new SimpleDateFormat(DATE_PATTERN, Locale.getDefault());
        try {
            if (sid == 0L) return null;
            object.put("sid", sid);
            object.put("devicetype", deviceType);
            if (scdList != null && scdList.size() > 0) {
                JSONArray scdArray = new JSONArray();
                for (FitnessSCD scd : scdList) {
                    JSONObject scdObj = new JSONObject();
                    scdObj.put("steps", (int) scd.getSteps());
                    scdObj.put("calorie", (int) scd.getCalories());
                    double distance = (double) Math.round(scd.getDistance() * 100.0) / 100.0;
                    scdObj.put("distance", distance);
                    scdObj.put("datacollectedts", format.format(new Date(scd.getLastUpdated())));
                    scdArray.put(scdObj);
                }
                object.put("scd", scdArray);
            }
            if (hrList != null && hrList.size() > 0) {
                JSONArray hrArray = new JSONArray();
                for (FitnessHR hr : hrList) {
                    JSONObject hrObj = new JSONObject();
                    hrObj.put("hr", hr.getHeartRate());
                    hrObj.put("datacollectedts", format.format(new Date(hr.getLastUpdated())));
                    hrArray.put(hrObj);
                }
                object.put("hr", hrArray);
            }
            if (bpList != null && bpList.size() > 0) {
                JSONArray bpArray = new JSONArray();
                for (FitnessBP bp : bpList) {
                    JSONObject bpObj = new JSONObject();
                    bpObj.put("systolic", bp.getSystolic());
                    bpObj.put("diastolic", bp.getDiastolic());
                    bpObj.put("datacollectedts", format.format(new Date(bp.getLastUpdated())));
                    bpArray.put(bpObj);
                }
                object.put("bp", bpArray);
            }
            return object;
        } catch (JSONException je) {
            Log.d(TAG, "json error", je);
            return null;
        }
    }

    @Nullable
    @Override
    public FanFitness fromJSONObject(JSONObject jsonObject) throws JSONException {
        JSONObject resp = jsonObject.optJSONObject("response");
        if (resp == null) return null;
        JSONArray jsonArray = resp.optJSONArray("result");
        if (jsonArray == null) return null;

        for (int index = 0; index < jsonArray.length(); index++) {
            JSONObject result = jsonArray.getJSONObject(index);
            int pageId = result.optInt("pageid", 1);
            int typeId = getLocalType(result.optInt("devicetypeid", -1));
            if (typeId == -1) return null;
            setPageId(pageId);
            JSONArray scdArray = result.optJSONArray("scd");
            if (scdArray != null && scdArray.length() > 0) {
                List<FitnessSCD> scdListTemp = new ArrayList<>();
                for (int i = 0; i < scdArray.length(); i++) {
                    FitnessSCD scd = getSCDFromJSon(scdArray.getJSONObject(i), typeId);
                    if (scd != null) {
                        scdListTemp.add(scd);
                    }

                }
                scdList = scdListTemp;
            }
            JSONArray hrArray = result.optJSONArray("hr");
            if (hrArray != null && hrArray.length() > 0) {
                List<FitnessHR> hrListTemp = new ArrayList<>();
                for (int i = 0; i < hrArray.length(); i++) {
                    FitnessHR hr = getHRFromJSon(hrArray.getJSONObject(i), typeId);
                    if (hr != null) {
                        hrListTemp.add(hr);
                    }
                }
                hrList = hrListTemp;
            }
            JSONArray bpArray = result.optJSONArray("bp");
            if (bpArray != null && bpArray.length() > 0) {
                List<FitnessBP> bpListTemp = new ArrayList<>();
                for (int i = 0; i < bpArray.length(); i++) {
                    FitnessBP bp = getBPFromJSon(bpArray.getJSONObject(i), typeId);
                    if (bp != null) {
                        bpListTemp.add(bp);
                    }
                }
                bpList = bpListTemp;
            }
            return this;
        }

        return null;
    }

    @Nullable
    private FitnessSCD getSCDFromJSon(JSONObject scdObj, int typeFomJson) {
        SimpleDateFormat format = new SimpleDateFormat(DATE_PATTERN1, Locale.getDefault());
        if (scdObj != null) {
            try {
                int steps = scdObj.optInt("steps", 0);
                int calorie = scdObj.optInt("calorie");
                double distance = scdObj.optDouble("distance", 0.0);
                String ts = scdObj.optString("datacollectedts");
                if (ts.isEmpty()) return null;
                Long tsLong = getTimeStamp(ts, format);
                if (tsLong == null) return null;
                if (steps > 0) {
                    return new FitnessSCD(0, typeFomJson,
                            steps, calorie, (float) distance, FitnessKt.DISTANCE_UNIT_KM, steps,
                            tsLong, System.currentTimeMillis());
                }
            } catch (ParseException pe) {
                Log.d(TAG, "parse error", pe);
            } catch (Exception ignore) {

            }
        }
        return null;
    }

    @Nullable
    private FitnessHR getHRFromJSon(JSONObject hrObj, int typeFomJson) {
        SimpleDateFormat format = new SimpleDateFormat(DATE_PATTERN1, Locale.getDefault());
        if (hrObj != null) {
            try {
                int heartRate = hrObj.optInt("hr", 0);
                String ts = hrObj.optString("datacollectedts");
                if (ts.isEmpty()) return null;
                Long tsLong = getTimeStamp(ts, format);
                if (tsLong == null) return null;
                if (heartRate > 0) {
                    return new FitnessHR(0, typeFomJson,
                            heartRate,
                            tsLong, System.currentTimeMillis());
                }
            } catch (ParseException pe) {
                Log.d(TAG, "parse error", pe);
            } catch (Exception ignore) {

            }
        }
        return null;
    }

    @Nullable
    private FitnessBP getBPFromJSon(JSONObject bpObj, int typeFomJson) {
        SimpleDateFormat format = new SimpleDateFormat(DATE_PATTERN1, Locale.getDefault());
        if (bpObj != null) {
            try {
                int systolic = bpObj.optInt("systolic", 0);
                int diastolic = bpObj.optInt("diastolic", 0);
                String ts = bpObj.optString("datacollectedts");
                if (ts.isEmpty()) return null;
                Long tsLong = getTimeStamp(ts, format);
                if (tsLong == null) return null;
                if (systolic > 0) {
                    return new FitnessBP(0, typeFomJson,
                            systolic, diastolic,
                            tsLong, System.currentTimeMillis());
                }
            } catch (ParseException pe) {
                Log.d(TAG, "parse error", pe);
            } catch (Exception ignore) {

            }
        }
        return null;
    }

}

/*
{
  "response": {
    "result": [
      {
        "pageid": 1,
        "scd": [
          {
            "steps": 99,
            "calorie": 3,
            "distance": 0.05,
            "datacollectedts": "2020-11-06T11:27:18"
          },


            hr - hr
            bp - systolic, diastolic
 */
