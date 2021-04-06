package com.fanplayiot.core.remote.pojo;

import android.util.Log;

import androidx.annotation.Nullable;

import com.fanplayiot.core.db.local.entity.Goal;
import com.fanplayiot.core.db.local.entity.Medical;
import com.fanplayiot.core.db.local.entity.PhysicalActivity;
import com.fanplayiot.core.db.local.entity.Profile;
import com.fanplayiot.core.db.local.entity.User;
import com.fanplayiot.core.db.local.entity.UserPref;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("UnusedAssignment")
public class UserDetails extends BaseData<UserDetails> {

    private static final String TAG = "UserDetails";
    private User user;
    /*private FanData fanData;
    private WaveData waveData;
    private WhistleData whistleData; */
    private Profile profile;
    /*
    {
        "sid": 30,
        "email": "emma@gmail.com",
        "displayname": "Emma",
        "rolename": "User",
        "teamid": 1,
        "favouriteteam": "CSK-IPL",
        "gender": "Male",
        "profileimage": "https://fangurudevstrg.blob.core.windows.net/userprofiles/common_dp.png",
        "mobilenumber": null,
        "dob": null,
        "ageinyears": 0,
        "city": "",
        "state": "",
        "country": null,
        "countrycode": null,
        "height": null,
        "heightmeasure": null,
        "weight": null,
        "weightmeasure": null,
        "devicemacid": null,
        "phonedeviceinfo": null,
        "bloodsugar": null,
        "bloodpressure": null,
        "heartrate": 0,
        "steps": 0,
        "calories": 0,
        "distance": 0,
        "sleepinghours": 0,
        "totalpoints": 0,
        "fescore": 0,
        "hrcount": 0,
        "tapcounts": 0,
        "wavecounts": 0,
        "whistlesredeemed": 0,
        "whistlecounts": 0,
        "cheeredteamid": 0,
        "affiliationname": null,
        "affiliationlogo": null,
        "active": true,
        "doyoufollowanyfitnessregimen": false,
        "interestedincustomizedfitnessregimen": false,
        "physicalactivities": [],
        "habits": [],
        "healthissues": [],
        "sports": []
      }
     */

    public User getUser() {
        return user;
    }

    /*public FanData getFanData() {
        return fanData;
    }

    public WaveData getWaveData() {
        return waveData;
    }

    public WhistleData getWhistleData() {
        return whistleData;
    }*/

    public Profile getProfile() {
        return profile;
    }

    @Override
    protected JSONObject getJSONObject() {
        return null;
    }

    @Override
    public @Nullable
    UserDetails fromJSONObject(JSONObject jsonObject) throws JSONException {
        JSONArray array = jsonObject.getJSONObject("response").getJSONArray("result");
        if (array.length() > 0) {
            //int teamId, tapCount, waveCount, whistleEarned, whistleRedeemed;
            long sid, teamIdServer = 1L; //, totalScore = 0;
            //float feScore;
            //final long lastUpdated = System.currentTimeMillis();
            JSONObject item = array.getJSONObject(0);
            User userObj = new User();
            userObj.setId(1);
            userObj.setTimeZone(GregorianCalendar.getInstance().getTimeZone().getDisplayName());
            sid = item.has("sid") ? item.getLong("sid") : -1;
            if (sid == -1) {
                Log.d(TAG, "sid is not available");
                return null;
            }
            userObj.setSid(sid);
            if (item.has("email") && !item.getString("email").equals("null")) {
                userObj.setEmail(item.getString("email"));
            }
            if (item.has("displayname") && !item.getString("displayname").equals("null")) {
                userObj.setProfileName(item.getString("displayname"));
            }
            // no rolename
            if (item.has("teamid")) {
                userObj.setTeamPref(item.getLong("teamid"));
            }
            if (item.has("cheeredteamid")) {
                teamIdServer = item.getInt("cheeredteamid");
                if (teamIdServer == 0) teamIdServer = 1L;
            }

            // no favouriteteam instead TeamPref is used
            if (item.has("gender") && !item.getString("gender").equals("null")) {
                userObj.setGender(item.getString("gender"));
            }
            if (item.has("profileimage") && !item.getString("profileimage").equals("null")) {
                userObj.setProfileImgUrl(item.getString("profileimage"));
            }
            if (item.has("mobilenumber") && !item.getString("mobilenumber").equals("null")) {
                userObj.setMobile(item.getString("mobilenumber"));
            }
            if (item.has("dob") && !item.getString("dob").equals("null")) {
                userObj.setDob(item.getString("dob"));
            }
            if (item.has("ageinyears")) {
                userObj.setAge(item.getInt("ageinyears"));
            }
            if (item.has("city") && !item.getString("city").equals("null")) {
                userObj.setCity(item.getString("city"));
            }
            if (item.has("height") && !item.getString("height").equals("null")) {
                userObj.setHeight(item.getString("height"));
            }
            if (item.has("heightmeasure") && !item.getString("heightmeasure").equals("null")) {
                userObj.setHeightMeasure(item.getString("heightmeasure"));
            }
            if (item.has("weight") && !item.getString("weight").equals("null")) {
                userObj.setWeight(item.getString("weight"));
            }
            if (item.has("weightmeasure") && !item.getString("weightmeasure").equals("null")) {
                userObj.setWeightMeasure(item.getString("weightmeasure"));
            }
            if (item.has("devicemacid") && !item.getString("devicemacid").equals("null")) {
                userObj.setDeviceId(item.getString("devicemacid"));
            }
            if (item.has("phonedeviceinfo") && !item.getString("phonedeviceinfo").equals("null")) {
                userObj.setPhoneDeviceInfo(item.getString("phonedeviceinfo"));
            }
            user = userObj;
            /*if (item.has("totalpoints")) {
                totalScore = item.getLong("totalpoints");
                feScore = (float) item.getDouble("fescore");
                tapCount = item.getInt("tapcounts");
                FanData fanDataObj = new FanData();
                fanDataObj.setTotalPoints(totalScore);
                fanDataObj.setFanMetric(feScore);
                fanDataObj.setTotalTapCount(tapCount);
                fanDataObj.setTeamId((int) teamIdServer);
                fanDataObj.setPlayerId(0);
                fanDataObj.setLastUpdated(lastUpdated);
                fanData = fanDataObj;
            }
            if (item.has("wavecounts")) {
                waveCount = item.getInt("wavecounts");
                WaveData waveDataObj = new WaveData();
                waveDataObj.setWaveCount(waveCount);
                waveDataObj.setWaveType(WaveData.PHONE);
                waveDataObj.setLastUpdated(lastUpdated);
                waveData = waveDataObj;
            }
            if (item.has("whistlesredeemed")) {
                whistleEarned = item.getInt("whistlecounts");
                whistleRedeemed = item.getInt("whistlesredeemed");
                WhistleData whistleDataObj = new WhistleData();
                whistleDataObj.setWhistleEarned(whistleEarned);
                whistleDataObj.setWhistleRedeemed(whistleRedeemed);
                whistleDataObj.setWhistleCount(Math.abs(whistleRedeemed - whistleEarned));
                whistleDataObj.setLastUpdated(lastUpdated);
                whistleData = whistleDataObj;
            }
            */
            // ignore hrcount

            // Profile related
            int hrMedical;
            String sugar, bp;
            Profile profileObj = new Profile();
            Medical medical = new Medical();
            if (item.has("bloodsugar") && !item.getString("bloodsugar").equals("null")) {
                sugar = item.optString("bloodsugar", "").trim();
                if (!sugar.isEmpty() && sugar.contains(medical.getBloodSugarUnit())) {
                    sugar = sugar.replace(medical.getBloodSugarUnit(), "").trim();
                }
                try {
                    medical.setBloodSugar(Integer.parseInt(sugar));
                } catch (NumberFormatException ignore) {

                }
            }
            if (item.has("bloodpressure") && !item.getString("bloodpressure").equals("null")) {
                bp = item.optString("bloodpressure", "").trim();
                if (!bp.isEmpty() && bp.contains(medical.getBloodPressureUnit())) {
                    bp = bp.replace(medical.getBloodPressureUnit(), "").trim();
                }
                String[] temp = bp.split("/");
                if (temp.length == 2) {
                    try {
                        medical.setBpSystolic(Integer.parseInt(temp[0]));
                        medical.setBpDiastolic(Integer.parseInt(temp[1]));
                    } catch (NumberFormatException ignore) {

                    }
                }
            }
            if (item.has("heartrate")) {
                hrMedical = item.optInt("heartrate", 60);
                if (hrMedical <= 0) hrMedical = 60;
                medical.setHeartRate(hrMedical);
            }
            if (item.has("healthissues")) {
                JSONArray healthArr = item.optJSONArray("healthissues");
                if (healthArr != null && healthArr.length() > 0) {
                    Set<String> healthSet = new HashSet<>();
                    for (int i = 0; i < healthArr.length(); i ++) {
                        healthSet.add( String.valueOf(
                                healthArr.getJSONObject(i).getInt("healthissueid")
                        ));
                    }
                    medical.setHealthIssues(healthSet);
                }
            }
            if (item.has("habits")) {
                JSONArray habitArr = item.optJSONArray("habits");
                if (habitArr != null && habitArr.length() > 0) {
                    Set<String> habitSet = new HashSet<>();
                    for (int i = 0; i < habitArr.length(); i ++) {
                        habitSet.add( String.valueOf(
                                habitArr.getJSONObject(i).getInt("habitid")
                        ));
                    }
                    medical.setHabits(habitSet);
                }
            }
            profileObj.setMedical(medical);

            Goal goal = new Goal();
            if (item.has("steps")) {
                int steps = item.optInt("steps", 10000);
                if (steps <= 0) steps = 10000;
                goal.setSteps(steps);
            }
            if (item.has("calories")) {
                int calories = item.optInt("calories", 1000);
                if (calories <= 0) calories = 1000;
                goal.setCalories(calories);
            }
            if (item.has("distance")) {
                int distance = item.optInt("distance", 3);
                if (distance <= 0) distance = 3;
                goal.setDistance(distance);
            }
            if (item.has("sleepinghours")) {
                int sleepHrs = item.optInt("sleepinghours", 7);
                if (sleepHrs <= 0) sleepHrs = 7;
                goal.setSleepHours(sleepHrs);
            }
            profileObj.setGoal(goal);

            UserPref pref = new UserPref();
            if (item.has("doyoufollowanyfitnessregimen")) {
                pref.setFollowFitness(item.optBoolean("doyoufollowanyfitnessregimen", false));
            }
            if (item.has("interestedincustomizedfitnessregimen")) {
                pref.setCustomFitness(item.optBoolean("interestedincustomizedfitnessregimen", false));
            }
            if (item.has("affiliationid")) {
                pref.setAffiliationId(item.optInt("affiliationid", 0));
            }
            pref.setTeamPrefId(teamIdServer);

            if (item.has("sports")) {
                JSONArray sportsArr = item.optJSONArray("sports");
                if (sportsArr != null && sportsArr.length() > 0) {
                    Set<String> sportsSet = new HashSet<>();
                    for (int i = 0; i < sportsArr.length(); i ++) {
                        sportsSet.add(String.valueOf(
                                sportsArr.getJSONObject(i).getInt("sportid")
                        ));
                    }
                    pref.setSportsIds(sportsSet);
                }
            }
            profileObj.setUserPref(pref);
            profileObj.setUser(userObj);

            if (item.has("physicalactivities")) {
                JSONArray paArr = item.optJSONArray("physicalactivities");
                if (paArr != null && paArr.length() > 0) {
                    List<PhysicalActivity> paSet = new ArrayList<>();
                    for (int i = 0 ; i < paArr.length(); i ++) {
                        JSONObject paObj = paArr.getJSONObject(i);
                        PhysicalActivity pa = new PhysicalActivity(
                            paObj.getInt("activityid"), paObj.optString("activityname", "null"));
                        if (paObj.has("perdayhour")) {
                            pa.setPerDayHour(paObj.optInt("perdayhour", 0));
                        }
                        if (paObj.has("perdaymin")) {
                            pa.setPerDayMin(paObj.optInt("perdaymin", 0));
                        }
                        if (paObj.has("perweekhour")) {
                            pa.setPerWeekHour(paObj.optInt("perweekhour", 0));
                        }
                        if (paObj.has("perweekmin")) {
                            pa.setPerWeekMin(paObj.optInt("perweekmin", 0));
                        }
                        paSet.add(pa);
                    }
                    profileObj.setPhysicalActivities(paSet);
                }
            }


            profile = profileObj;
            return this;
        }
        return null;
    }

}
