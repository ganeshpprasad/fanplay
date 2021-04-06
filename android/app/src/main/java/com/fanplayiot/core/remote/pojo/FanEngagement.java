package com.fanplayiot.core.remote.pojo;

import android.util.Log;

import androidx.annotation.NonNull;

import com.fanplayiot.core.db.local.entity.Device;
import com.fanplayiot.core.db.local.entity.FanData;
import com.fanplayiot.core.db.local.entity.HeartRate;
import com.fanplayiot.core.db.local.entity.Player;
import com.fanplayiot.core.db.local.entity.User;
import com.fanplayiot.core.db.local.entity.WaveData;
import com.fanplayiot.core.db.local.entity.WhistleData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FanEngagement extends BaseData<FanEngagement> {
    /*
    {
  "hrcount": 0,
  "hrdevicetype": 0,
  "datacollectedts": "2020-09-03T06:50:29.683Z",
  "teamcheered": 0,
  "tapcounts": 0,
  "wavecounts": 0,
  "whistlesredeemed": 0,
  "whistlecounts": 0,
  "fescore": 0,
  "points": 0,
  "latitude": 0,
  "longitude": 0,
  "devicemacid": "string",
  "playertapcheer": [
    {
      "playerid": 0,
      "tapvalue": 0
    }
  ],
  "playerwavecheer": [
    {
      "playerid": 0,
      "wavevalue": 0
    }
  ],
  "playerwhistleredeemed": [
    {
      "playerid": 0,
      "wrvalue": 0
    }
  ]
}

     */
    private static final String TAG = "FanEngagment";
    private HeartRate heartRate;
    private FanData fanData;
    private WaveData waveData;
    private WhistleData whistleData;
    private Player[] players;
    private User user;
    private Device device;
    private long teamIdCheered = 1L;
    private int hrZone = 3;
    private int affiliationId = 0;

    public FanEngagement() {
    }

    public void setHeartRate(@NonNull HeartRate heartRate) {
        this.heartRate = heartRate;
    }

    public void setFanData(@NonNull FanData fanData) {
        this.fanData = fanData;
    }

    public void setWaveData(@NonNull WaveData waveData) {
        this.waveData = waveData;
    }

    public void setWhistleData(@NonNull WhistleData whistleData) {
        this.whistleData = whistleData;
    }

    public void setPlayers(@NonNull Player[] array) {
        this.players = array;
        //Log.d(TAG, "array length " + array.length);
    }

    public void setUser(@NonNull User user) {
        this.user = user;
    }

    public void setDevice(@NonNull Device device) {
        this.device = device;
    }

    public void setTeamIdCheered(long teamIdCheered) {
        this.teamIdCheered = teamIdCheered;
    }

    public void setHrZone(int hrZone) {
        this.hrZone = hrZone;
    }

    public void setAffiliationId(long affiliationId) {
        this.affiliationId = (int)affiliationId;
    }

    @Override
    public JSONObject getJSONObject() {
        JSONObject object = new JSONObject();
        try {
            String macAddress = "00:00:00:00:00:00";
            int deviceType = 0;
            int hrCount = 0, tapCount = 0, waveCount = 0, whistleCount = 0, whistleRedeemed = 0;
            float fanMetric = 0f;
            long points = 0;
            double latitude = 0, longitude = 0;
            if (heartRate != null) {
                hrCount = heartRate.getHeartRate();
                if (heartRate.getType() == HeartRate.DEVICE_BAND) {
                    deviceType = 3;
                } else if (heartRate.getType() == HeartRate.CAMERA) {
                    deviceType = 1;
                } else {
                    deviceType = 2;
                }

            }
            if (fanData != null) {
                DecimalFormat df = new DecimalFormat();
                df.setMaximumFractionDigits(1);
                tapCount = fanData.getTotalTapCount();
                fanMetric = fanData.getFanMetric();
                points = fanData.getTotalPoints();
            }
            if (waveData != null) {
                waveCount = waveData.getWaveCount();
            }
            if (whistleData != null) {
                whistleCount = whistleData.getWhistleEarned();
                whistleRedeemed = whistleData.getWhistleRedeemed();
                Log.d(TAG, "whistleCount " + whistleCount + " whistleRedeemed " + whistleRedeemed);
            }
            if (device != null && heartRate != null && heartRate.getType() == HeartRate.DEVICE_BAND) {
                macAddress = device.getAddress();
            } else {
                if (user != null && user.getDeviceId() != null) macAddress = user.getDeviceId();
            }
            if (user != null) {
                latitude = user.getLatitude();
                longitude = user.getLongitude();
                //Log.d(TAG, "latitude " + latitude + " longitude " + longitude);
            }
            String ts = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                    .format(new Date(System.currentTimeMillis()));

            object.put("hrcount", hrCount);
            object.put("hrdevicetype", deviceType);
            object.put("datacollectedts", ts); //"2020-09-05T11:15:36.464Z");
            object.put("teamcheered", teamIdCheered); // CSK team id = 1
            object.put("tapcounts", tapCount);
            object.put("wavecounts", waveCount);
            object.put("whistlesredeemed", whistleRedeemed);
            object.put("whistlecounts", whistleCount);
            object.put("fescore", (double) Math.round(fanMetric * 10.0) / 10.0);
            object.put("points", points);
            object.put("latitude", latitude);
            object.put("longitude", longitude);
            object.put("devicemacid", macAddress);
            object.put("hrzoneid", hrZone > 0 ? hrZone : null);
            object.put("affiliationid", affiliationId > 0 ? affiliationId : null);
            if (players == null) {
                players = new Player[0];
            }
            JSONArray playerTapArray = new JSONArray();
            for (Player player : players) {
                if (player.getTapCount() > 0) {
                    JSONObject tapPlayer = new JSONObject();
                    tapPlayer.put("playerid", player.getId());
                    tapPlayer.put("tapvalue", player.getTapCount());
                    playerTapArray.put(tapPlayer);
                }
            }
            object.put("playertapcheer", playerTapArray);

            JSONArray playerWaveArray = new JSONArray();
            for (Player player : players) {
                if (player.getWaveCount() > 0) {
                    JSONObject wavePlayer = new JSONObject();
                    wavePlayer.put("playerid", player.getId());
                    wavePlayer.put("wavevalue", player.getWaveCount());
                    playerWaveArray.put(wavePlayer);
                }
            }
            object.put("playerwavecheer", playerWaveArray);

            JSONArray playerWhistleArray = new JSONArray();
            for (Player player : players) {
                if (player.getWhistleCount() > 0) {
                    JSONObject whistlePlayer = new JSONObject();
                    whistlePlayer.put("playerid", player.getId());
                    whistlePlayer.put("wrvalue", player.getWhistleCount());
                    playerWhistleArray.put(whistlePlayer);
                }
            }
            object.put("playerwhistleredeemed", playerWhistleArray);

        } catch (JSONException je) {
            Log.d(TAG, "json error", je);
            return null;
        }

        return object;
    }

    @SuppressWarnings("RedundantThrows")
    @Override
    protected FanEngagement fromJSONObject(JSONObject jsonObject) throws JSONException {
        return null;
    }
}
