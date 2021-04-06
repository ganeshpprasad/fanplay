package com.fanplayiot.core.db.local.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.util.Log;
import android.util.Patterns;

import androidx.annotation.ArrayRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.demoproject.R;
import com.fanplayiot.core.bluetooth.SDKManager;
import com.fanplayiot.core.db.local.FanplayiotDatabase;
import com.fanplayiot.core.db.local.dao.HomeDao;
import com.fanplayiot.core.db.local.entity.FanData;
import com.fanplayiot.core.db.local.entity.Goal;
import com.fanplayiot.core.db.local.entity.Medical;
import com.fanplayiot.core.db.local.entity.Profile;
import com.fanplayiot.core.db.local.entity.Team;
import com.fanplayiot.core.db.local.entity.User;
import com.fanplayiot.core.db.local.entity.UserPref;
import com.fanplayiot.core.remote.pojo.AllHabits;
import com.fanplayiot.core.remote.pojo.AllHealthIssues;
import com.fanplayiot.core.remote.pojo.AllPhysicalActivity;
import com.fanplayiot.core.remote.pojo.AllSports;
import com.fanplayiot.core.remote.pojo.NameLogo;
import com.fanplayiot.core.remote.repository.AdvertiserRepository;
import com.fanplayiot.core.remote.repository.TeamRepository;
import com.fanplayiot.core.utils.Constant;
import com.mcube.ms.sdk.definitions.MSDefinition;

import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.fanplayiot.core.db.ConstantKt.HR_ZONE_50_KEY;
import static com.fanplayiot.core.db.ConstantKt.HR_ZONE_60_KEY;
import static com.fanplayiot.core.db.ConstantKt.HR_ZONE_70_KEY;
import static com.fanplayiot.core.db.ConstantKt.HR_ZONE_80_KEY;
import static com.fanplayiot.core.db.local.dao.ScoreHelper.MHR_ZONE_50;
import static com.fanplayiot.core.db.local.dao.ScoreHelper.MHR_ZONE_60;
import static com.fanplayiot.core.db.local.dao.ScoreHelper.MHR_ZONE_70;
import static com.fanplayiot.core.db.local.dao.ScoreHelper.MHR_ZONE_80;
import static com.fanplayiot.core.viewmodel.MainProfileViewModel.CM;
import static com.fanplayiot.core.viewmodel.MainProfileViewModel.FEET;
import static com.fanplayiot.core.viewmodel.MainProfileViewModel.KG;
import static com.fanplayiot.core.viewmodel.MainProfileViewModel.LB;
import static com.fanplayiot.core.utils.Constant.AFFILIATION_KEY;
import static com.fanplayiot.core.utils.Constant.AGE_KEY;
import static com.fanplayiot.core.utils.Constant.ALL_MASTER_KEY;
import static com.fanplayiot.core.utils.Constant.BLOOD_SUGAR_KEY;
import static com.fanplayiot.core.utils.Constant.BP_DIASTOLIC_KEY;
import static com.fanplayiot.core.utils.Constant.BP_SYSTOLIC_KEY;
import static com.fanplayiot.core.utils.Constant.CUSTOM_FITNESS_KEY;
import static com.fanplayiot.core.utils.Constant.FAV_SPORTS_KEY;
import static com.fanplayiot.core.utils.Constant.FCM_TOKEN;
import static com.fanplayiot.core.utils.Constant.FOLLOW_FITNESS_KEY;
import static com.fanplayiot.core.utils.Constant.GENDER_KEY;
import static com.fanplayiot.core.utils.Constant.GOAL_CALORIES_KEY;
import static com.fanplayiot.core.utils.Constant.GOAL_DISTANCE_KEY;
import static com.fanplayiot.core.utils.Constant.GOAL_SLEEP_KEY;
import static com.fanplayiot.core.utils.Constant.GOAL_STEPS_KEY;
import static com.fanplayiot.core.utils.Constant.HABITS_KEY;
import static com.fanplayiot.core.utils.Constant.HEALTH_ISSUES_KEY;
import static com.fanplayiot.core.utils.Constant.HEART_RATE_KEY;
import static com.fanplayiot.core.utils.Constant.HEIGHT_KEY;
import static com.fanplayiot.core.utils.Constant.MASTER_UPDATED_KEY;
import static com.fanplayiot.core.utils.Constant.PHYSICAL_ACTIVITY_KEY;
import static com.fanplayiot.core.utils.Constant.POSTURE_KEY;
import static com.fanplayiot.core.utils.Constant.PREF_FILE_KEY;
import static com.fanplayiot.core.utils.Constant.PREF_PROFILE;
import static com.fanplayiot.core.utils.Constant.REFERRED_BY;
import static com.fanplayiot.core.utils.Constant.STEPS_KEY;
import static com.fanplayiot.core.utils.Constant.TEAM_PREF_KEY;
import static com.fanplayiot.core.utils.Constant.WEIGHT_KEY;

public class UserProfileStorage {
    private static final String TAG = "UserProfileStorage";
    private static String MALE = "Male";
    private static String FEMALE = "Female";
    public LiveData<User> userLive;
    public LiveData<String> fanPicture;
    public LiveData<FanData> fanDataLive;
    private HomeDao dao;
    private final SharedPreferences sp;
    private final SharedPreferences profileSharedPref;
    private boolean isMasterUpdated;
    private Resources resources;
    private Context context;

    private SDKManager sdk = SDKManager.instance(context);
    public UserProfileStorage(Context context) {
        MALE = "male";
        FEMALE = "female";
        this.context = context;
        sp = context.getSharedPreferences(PREF_FILE_KEY, Context.MODE_PRIVATE);
        profileSharedPref = context.getSharedPreferences(PREF_PROFILE, Context.MODE_PRIVATE);
        resources = context.getResources();
        isMasterUpdated = sp.getBoolean(MASTER_UPDATED_KEY, false);
        FanplayiotDatabase db = FanplayiotDatabase.getDatabase(context);
        dao = db.homeDao();
        userLive = dao.getUserLive();
        fanPicture = dao.getImageUrl();
        fanDataLive = dao.getFanDataLive();
    }

    public UserProfileStorage(@NonNull Context context, @NonNull HomeDao homeDao) {
        this(context);
        dao = homeDao;
    }

    public void updateProfilePic(@NonNull final String url) {
        FanplayiotDatabase.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    User user = dao.getUserData();
                    if (user != null) {
                        //Log.d(TAG, "updateProfilePic " + url);
                        if (Patterns.WEB_URL.matcher(url).matches() && !url.endsWith("common_dp.png")) {
                            user.setProfileImgUrl(url);
                            dao.update(user);
                            return;
                        }
                        String filePath = Uri.parse(url).getPath();
                        //Log.d(TAG, "updateProfilePic " + filePath);
                        File imagePath = new File(filePath);
                        if (imagePath.exists()) {
                            user.setProfileImgUrl(url);
                            dao.update(user);
                        }
                    } else {
                        Log.d(TAG, "User is null");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "error ", e);
                }
            }
        });
    }

    public LiveData<Boolean> updateProfile(@NonNull final User user, @Nullable final String filePath) {
        final MutableLiveData<Boolean> updateSuccess = new MutableLiveData<>(false);
        FanplayiotDatabase.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    User prev = dao.getUserData();
                    if (prev != null) {
                        dao.update(user);
                        updateSuccess.postValue(true);
                    } else {
                        Log.d(TAG, "User is null");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "error ", e);
                }
            }
        });
        return updateSuccess;
    }

    public void updateUser(@NonNull final User user) {
        FanplayiotDatabase.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    User prev = dao.getUserData();
                    if (prev != null) {
                        // Update Shared preference for profile used in MS SDK
                        SharedPreferences.Editor editor = profileSharedPref.edit();

                        // Gender
                        if (user.getGender() != null) {
                            if (user.getGender().equalsIgnoreCase(FEMALE)) {
                                editor.putInt(Constant.GENDER_KEY, MSDefinition.GENDER_FEMALE);
                            } else {
                                editor.putInt(Constant.GENDER_KEY, MSDefinition.GENDER_MALE);
                            }
                        } else {
                            editor.putInt(Constant.GENDER_KEY, MSDefinition.GENDER_MALE);
                        }

                        // Height
                        if (user.getHeight() != null) {
                            try {
                                float heightValue = Float.parseFloat(user.getHeight());
                                String heightMeasure = user.getHeightMeasure();
                                if (CM.equalsIgnoreCase(heightMeasure)) {
                                    editor.putInt(Constant.HEIGHT_KEY, (int) heightValue);
                                } else if (FEET.equalsIgnoreCase(heightMeasure)) {
                                    editor.putInt(Constant.HEIGHT_KEY, (int) (heightValue * 30.48));
                                } else {
                                    editor.putInt(Constant.HEIGHT_KEY, (int) heightValue);
                                }
                            } catch (NumberFormatException nfe) {
                                editor.putInt(Constant.HEIGHT_KEY, MSDefinition.HEIGHT_DEFAULT_VALUE);
                            }
                        } else {
                            editor.putInt(Constant.HEIGHT_KEY, MSDefinition.HEIGHT_DEFAULT_VALUE);
                        }

                        // Weight
                        if (user.getWeight() != null) {
                            try {
                                float weightValue = Float.parseFloat(user.getWeight());
                                String weightMeasure = user.getWeightMeasure();
                                if (KG.equalsIgnoreCase(weightMeasure)) {
                                    editor.putInt(Constant.WEIGHT_KEY, (int) weightValue);
                                } else if (LB.equalsIgnoreCase(weightMeasure)) {
                                    editor.putInt(Constant.WEIGHT_KEY, (int) Math.round(weightValue / 2.205));
                                } else {
                                    editor.putInt(Constant.WEIGHT_KEY, (int) weightValue);
                                }
                            } catch (NumberFormatException nfe) {
                                editor.putInt(Constant.WEIGHT_KEY, MSDefinition.WEIGHT_DEFAULT_VALUE);
                            }
                        } else {
                            editor.putInt(Constant.WEIGHT_KEY, MSDefinition.WEIGHT_DEFAULT_VALUE);
                        }

                        // Age
                        if (user.getAge() > 0) {
                            editor.putInt(Constant.AGE_KEY, user.getAge());
                        } else {
                            editor.putInt(Constant.AGE_KEY, MSDefinition.AGE_DEFAULT_VALUE);
                        }
                        editor.putInt(Constant.POSTURE_KEY, MSDefinition.POSTURE_RIGHT);
                        editor.apply();

                        user.setLastUpdated(System.currentTimeMillis());
                        dao.update(user);
                        updateUserModule();
                    } else {
                        Log.d(TAG, "User is null");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "error ", e);
                }
            }
        });
    }

    public void updateUserModule() {
        int gender = profileSharedPref.getInt(GENDER_KEY, MSDefinition.GENDER_MALE);
        int age = profileSharedPref.getInt(AGE_KEY, MSDefinition.AGE_DEFAULT_VALUE);
        int height = profileSharedPref.getInt(HEIGHT_KEY, MSDefinition.HEIGHT_DEFAULT_VALUE);
        int weight = profileSharedPref.getInt(WEIGHT_KEY, MSDefinition.WEIGHT_DEFAULT_VALUE);
        int posture = profileSharedPref.getInt(POSTURE_KEY, MSDefinition.POSTURE_RIGHT);
        int unit = MSDefinition.UNIT_KILOMETERS;
        int steps = profileSharedPref.getInt(STEPS_KEY, MSDefinition.TARGET_STEPS_DEFAULT);

        boolean success = sdk.getUserModule().setProfile(gender, age, height, weight, posture, unit, steps);
        Log.d(TAG, "User Module in SDK update " + success);
    }

    public void updateMasters(@NonNull String strJson) {
        isMasterUpdated = sp.getBoolean(MASTER_UPDATED_KEY, false);
        String allMasterStr = sp.getString(ALL_MASTER_KEY, "");
        if (!allMasterStr.isEmpty() && allMasterStr.equals(strJson)) {
            Log.d(TAG, "All Master already updated");
            return;
        } else if (!allMasterStr.isEmpty()) {
            clearMasters();
        }
        sp.edit()
                .putString(ALL_MASTER_KEY, strJson)
                .putBoolean(MASTER_UPDATED_KEY, true)
                .apply();
        Log.d(TAG, "All Master created");
    }

    public void clearMasters() {
        sp.edit()
                .remove(ALL_MASTER_KEY)
                .putBoolean(MASTER_UPDATED_KEY, false)
                .apply();
        Log.d(TAG, "All Master cleared");
    }

    public @NonNull
    List<NameLogo> getAllSports() {
        try {
            String allMasterStr = sp.getString(ALL_MASTER_KEY, "");
            if (!allMasterStr.isEmpty()) {
                AllSports sports = AllSports.getInstance(AllSports.class, sp.getString(ALL_MASTER_KEY, ""));
                List<NameLogo> list = sports.getBaseItemList();
                if (list != null && list.size() > 0) return list;
            }
        } catch (IllegalAccessException | InstantiationException | JSONException e) {
            Log.e(TAG, "error ", e);
        }
        return getFromResourceArray(R.array.sports);
    }

    public @NonNull
    List<NameLogo> getAllHealthIssues() {
        try {
            String allMasterStr = sp.getString(ALL_MASTER_KEY, "");
            if (!allMasterStr.isEmpty()) {
                AllHealthIssues allHealthIssues = AllHealthIssues.getInstance(AllHealthIssues.class, sp.getString(ALL_MASTER_KEY, ""));
                List<NameLogo> list = allHealthIssues.getBaseItemList();
                if (list != null && list.size() > 0) return list;
            }
        } catch (IllegalAccessException | InstantiationException | JSONException e) {
            Log.e(TAG, "error ", e);
        }
        return getFromResourceArray(R.array.health_issues);
    }

    public @NonNull
    List<NameLogo> getAllHabits() {
        try {
            String allMasterStr = sp.getString(ALL_MASTER_KEY, "");
            if (!allMasterStr.isEmpty()) {
                AllHabits allHabits = AllHabits.getInstance(AllHabits.class, sp.getString(ALL_MASTER_KEY, ""));
                List<NameLogo> list = allHabits.getBaseItemList();
                if (list != null && list.size() > 0) return list;
            }
        } catch (IllegalAccessException | InstantiationException | JSONException e) {
            Log.e(TAG, "error ", e);
        }
        return getFromResourceArray(R.array.habits);
    }

    public @NonNull
    List<NameLogo> getAllPhysicalActivities() {
        try {
            String allMasterStr = sp.getString(ALL_MASTER_KEY, "");
            if (!allMasterStr.isEmpty()) {
                AllPhysicalActivity paAll = AllPhysicalActivity.getInstance(AllPhysicalActivity.class, sp.getString(ALL_MASTER_KEY, ""));
                List<NameLogo> list = paAll.getBaseItemList();
                if (list != null && list.size() > 0) return list;
            }
        } catch (IllegalAccessException | InstantiationException | JSONException e) {
            Log.e(TAG, "error ", e);
        }
        return getFromResourceArray(R.array.physical_activities);
    }

    public Profile getProfile(@NonNull User user) {
        Profile profile = new Profile();

        UserPref pref = new UserPref();
        pref.setTeamPrefId(sp.getLong(TEAM_PREF_KEY, 1L));
        pref.setAffiliationId(sp.getLong(AFFILIATION_KEY, 0L));
        pref.setSportsIds(sp.getStringSet(FAV_SPORTS_KEY, UserPref.getDefaultSportsIds()));
        pref.setFollowFitness(sp.getBoolean(FOLLOW_FITNESS_KEY, false));
        pref.setCustomFitness(sp.getBoolean(CUSTOM_FITNESS_KEY, false));

        Medical medical = new Medical(
                sp.getStringSet(HEALTH_ISSUES_KEY, new HashSet<String>()),
                sp.getStringSet(HABITS_KEY, new HashSet<String>()),
                sp.getInt(BLOOD_SUGAR_KEY, 100), // If no value : Blood Sugar (Random): 100 mg/dl
                sp.getInt(BP_SYSTOLIC_KEY, 120), // If no value : Blood Pressure: 120/80 mmHg
                sp.getInt(BP_DIASTOLIC_KEY, 80),
                sp.getInt(HEART_RATE_KEY, 60) // If no value : Heart Rate : 60
        );

        Goal goal = new Goal(
                sp.getLong(GOAL_STEPS_KEY, 10000L), // If no value : Steps : 10000
                sp.getInt(GOAL_CALORIES_KEY, 1000), // If no value : Calories : 1,000 calories per day
                sp.getInt(GOAL_DISTANCE_KEY, 3),
                (int) sp.getLong(GOAL_SLEEP_KEY, 7L) // If no value : Sleeping hours: 7 Hrs
        );

        profile.setUser(user);
        profile.setUserPref(pref);
        profile.setMedical(medical);
        profile.setGoal(goal);
        profile.setPhysicalActivities(sp.getStringSet(PHYSICAL_ACTIVITY_KEY, new HashSet<String>()));
        return profile;
    }

    public void updateProfile(Profile profile) {
        User user = profile.getUser();
        int ageSel = 32;
        if (user != null) {
            ageSel = profile.getUser().getAge();
        }
        UserPref userPref = profile.getUserPref();
        Medical medical = profile.getMedical();
        Goal goal = profile.getGoal();
        sp.edit()
                .putFloat(HR_ZONE_50_KEY, (220 - ageSel) * MHR_ZONE_50)
                .putFloat(HR_ZONE_60_KEY, (220 - ageSel) * MHR_ZONE_60)
                .putFloat(HR_ZONE_70_KEY, (220 - ageSel) * MHR_ZONE_70)
                .putFloat(HR_ZONE_80_KEY, (220 - ageSel) * MHR_ZONE_80)

                .putLong(AFFILIATION_KEY, userPref.getAffiliationId())
                .putLong(TEAM_PREF_KEY, userPref.getTeamPrefId())
                .putStringSet(FAV_SPORTS_KEY, userPref.getSportsIds())
                .putBoolean(FOLLOW_FITNESS_KEY, userPref.isFollowFitness())
                .putBoolean(CUSTOM_FITNESS_KEY, userPref.isCustomFitness())

                .putStringSet(HEALTH_ISSUES_KEY, medical.getHealthIssues())
                .putStringSet(HABITS_KEY, medical.getHabits())
                .putInt(BLOOD_SUGAR_KEY, medical.getBloodSugar())
                .putInt(BP_SYSTOLIC_KEY, medical.getBpSystolic())
                .putInt(BP_DIASTOLIC_KEY, medical.getBpDiastolic())
                .putInt(HEART_RATE_KEY, medical.getHeartRate())

                .putStringSet(PHYSICAL_ACTIVITY_KEY, profile.getStringSetPhysicalActivities())
                .putLong(GOAL_STEPS_KEY, goal.getSteps())
                .putInt(GOAL_CALORIES_KEY, goal.getCalories())
                .putInt(GOAL_DISTANCE_KEY, goal.getDistance())
                .putLong(GOAL_SLEEP_KEY, (long) goal.getSleepHours())
                .apply();

        // MS SDK profile set the steps goal
        SharedPreferences.Editor editor = profileSharedPref.edit();
        editor.putInt(Constant.STEPS_KEY, (int) goal.getSteps());
        editor.apply();

        if (user != null) {
            updateUser(user);
        }
    }

    private @NonNull
    List<NameLogo> getFromResourceArray(@ArrayRes int arrayId) {
        String[] array = resources.getStringArray(arrayId);
        List<NameLogo> list = new ArrayList<>();
        for (int i = 0; i < array.length; i++) {
            list.add(new NameLogo(i + 1, array[i], null));
        }
        return list;
    }

    public @Nullable
    Set<Integer> getIntegerSet(@Nullable Set<String> stringSet) {
        if (stringSet == null) return null;
        Set<Integer> intSet = new HashSet<>();
        for (String s : stringSet) {
            try {
                int value = Integer.parseInt(s);
                intSet.add(value);
            } catch (NumberFormatException ignore) {

            }
        }
        return intSet;
    }

    public @Nullable
    Set<String> getStringSet(@Nullable Set<Integer> intSet) {
        if (intSet == null) return null;
        Set<String> stringSet = new HashSet<>();
        for (int intValue : intSet) {
            stringSet.add(String.valueOf(intValue));
        }
        return stringSet;
    }

    public void setDefaultTeam(long teamSel, String teamName) {
        FanplayiotDatabase.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // Update Team
                    long oldTeamIdServer = -1L;
                    Team[] teams = dao.getAllTeam();
                    HomeRepository homeRepository = new HomeRepository(context);
                    TeamRepository teamRepository = new TeamRepository(context, homeRepository);
                    if (teams != null && teams.length > 0) {
                        Team currTeam = teams[0];
                        oldTeamIdServer = currTeam.getTeamIdServer();
                        currTeam.setTeamName(teamName);
                        currTeam.setTeamIdServer(teamSel);
                        dao.update(currTeam);
                    } else {
                        Team newTeam = new Team();
                        newTeam.setId(1);
                        newTeam.setTeamName(teamName);
                        newTeam.setTeamIdServer(teamSel);
                        dao.insert(newTeam);
                    }
                    Team defaultTeam = dao.getDefaultTeam();
                    if (defaultTeam != null) {
                        teamRepository.getTeamAndPlayers(defaultTeam);
                    }
                    SponsorRepository sponsorRepository = new SponsorRepository(context);
                    AdvertiserRepository advertiserRepository = new AdvertiserRepository(context, sponsorRepository);
                    advertiserRepository.postAnalyticsAndGetSponsors();

                    /*
                     Check if already old default team is available. if already available fetch
                     total tap, total wave, total whistle, fescore and points for the new team
                     from backend server
                    */
                    if (oldTeamIdServer != -1L && oldTeamIdServer != teamSel) {
                        homeRepository.getFanEngageData();
                        Log.d(TAG, "Team changed. getting fan engage details ...");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "error in set default team", e);
                }
            }
        });
    }

    public void updateTeamId(Long teamIdServer) {
        if (teamIdServer != null && teamIdServer > 0L) {
            sp.edit().putLong(TEAM_PREF_KEY, teamIdServer).apply();
        }
    }

    public Long getAffiliationId() { return sp.getLong(AFFILIATION_KEY, 0L); }

    public LiveData<Team> getDefaultTeam() {
        return dao.getTeamLive();
    }

    public void setReferredBySid(long sid) {
        sp.edit().putLong(REFERRED_BY, sid).apply();
    }
    public boolean isReferred() {
        return sp.getLong(REFERRED_BY, -1L) != -1L;
    }

    public void setFCMToken(String token) {
        sp.edit().putString(FCM_TOKEN, token).apply();
    }
    public String getFCMToken() {
        return sp.getString(FCM_TOKEN, "");
    }
}