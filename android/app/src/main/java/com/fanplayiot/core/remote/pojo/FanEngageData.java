package com.fanplayiot.core.remote.pojo;

import com.fanplayiot.core.db.local.entity.FanData;
import com.fanplayiot.core.db.local.entity.WaveData;
import com.fanplayiot.core.db.local.entity.WhistleData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FanEngageData extends BaseData<FanEngageData> {
    private FanData fanData;
    private WaveData waveData;
    private WhistleData whistleData;

    public FanData getFanData() {
        return fanData;
    }

    public WaveData getWaveData() {
        return waveData;
    }

    public WhistleData getWhistleData() {
        return whistleData;
    }

    @Override
    protected JSONObject getJSONObject() {
        return null;
    }

    @Override
    public FanEngageData fromJSONObject(JSONObject jsonObject) throws JSONException {
        // Check for null or no values
        if (jsonObject.optJSONObject("response") == null) return null;
        if (jsonObject.getJSONObject("response").optJSONArray("result") == null) return null;
        JSONArray jsonArray = jsonObject.getJSONObject("response").getJSONArray("result");
        if (jsonArray.length() == 0) return null;
        JSONObject item = jsonArray.optJSONObject(0);
        if (item == null) return null;

        int tapCount, waveCount, whistleEarned, whistleRedeemed;
        long totalScore;
        float feScore;
        final long lastUpdated = System.currentTimeMillis();
        if (item.has("points") && item.has("tapcounts")) {
            totalScore = item.optLong("points", 0L);
            feScore = (float) item.optDouble("fescore", 0f);
            tapCount = item.optInt("tapcounts", 0);
            FanData fanDataObj = new FanData();
            fanDataObj.setTotalPoints(totalScore);
            fanDataObj.setFanMetric(feScore);
            fanDataObj.setTotalTapCount(tapCount);
            fanDataObj.setTeamId(1);
            fanDataObj.setPlayerId(0);
            fanDataObj.setLastUpdated(lastUpdated);
            fanDataObj.setLastSynced(lastUpdated);
            fanData = fanDataObj;
        }
        if (item.has("wavecounts")) {
            waveCount = item.optInt("wavecounts", 0);
            WaveData waveDataObj = new WaveData();
            waveDataObj.setWaveCount(waveCount);
            waveDataObj.setWaveType(WaveData.PHONE);
            waveDataObj.setLastUpdated(lastUpdated);
            waveData = waveDataObj;
        }
        if (item.has("whistlesredeemed")) {
            whistleEarned = item.optInt("whistlecounts", 0);
            whistleRedeemed = item.optInt("whistlesredeemed", 0);
            WhistleData whistleDataObj = new WhistleData();
            whistleDataObj.setWhistleEarned(whistleEarned);
            whistleDataObj.setWhistleRedeemed(whistleRedeemed);
            whistleDataObj.setWhistleCount(Math.abs(whistleRedeemed - whistleEarned));
            whistleDataObj.setLastUpdated(lastUpdated);
            whistleData = whistleDataObj;
        }

        return this;
    }

    /*
    {"response":{"result":[
        {"tapcounts":0,
        "wavecounts":0,
        "whistlesredeemed":0,
        "whistlecounts":1,
        "fescore":6.3,
        "points":110}]}
     */
}
