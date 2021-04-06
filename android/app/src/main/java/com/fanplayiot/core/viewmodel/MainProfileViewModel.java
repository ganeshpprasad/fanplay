package com.fanplayiot.core.viewmodel;

import android.app.Application;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.fanplayiot.core.db.local.entity.FanData;
import com.fanplayiot.core.db.local.entity.Medical;
import com.fanplayiot.core.db.local.entity.PhysicalActivity;
import com.fanplayiot.core.db.local.entity.Profile;
import com.fanplayiot.core.db.local.entity.Team;
import com.fanplayiot.core.db.local.entity.User;
import com.fanplayiot.core.db.local.entity.UserPref;
import com.fanplayiot.core.db.local.repository.UserProfileStorage;
import com.fanplayiot.core.remote.FirebaseAuthService;
import com.fanplayiot.core.remote.firebase.analytics.AnalyticsService;
import com.fanplayiot.core.remote.pojo.NameLogo;
import com.fanplayiot.core.remote.repository.ProfileRepository;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainProfileViewModel extends AndroidViewModel {

    private static final String TAG = "MainProfileViewModel";
    public static final float HEIGHT_CM_DEFAULT = 165f;
    public static final float HEIGHT_CM_MIN = 100f;
    public static final float HEIGHT_CM_MAX = 220f;
    public static final float HEIGHT_FT_DEFAULT = 5.5f;
    public static final float HEIGHT_FT_MIN = 3.00f;
    public static final float HEIGHT_FT_MAX = 7.5f;
    public static final float WEIGHT_KG_DEFAULT = 58f;
    public static final float WEIGHT_KG_MIN = 35f;
    public static final float WEIGHT_KG_MAX = 130f;
    public static final float WEIGHT_LB_DEFAULT = 125f;
    public static final float WEIGHT_LB_MIN = 75f;
    public static final float WEIGHT_LB_MAX = 290f;
    public static final int AGE_DEFAULT = 28;
    public static final String CM = "cm";
    public static final String FEET = "feet";
    public static final String KG = "kg";
    public static final String LB = "lb";

    private final UserProfileStorage repository;
    private final ProfileRepository profileRepository;
    public LiveData<User> userLive;
    public LiveData<Profile> profileLive;
    public LiveData<String> fanPicture;
    public LiveData<FanData> fanDataLive;
    public LiveData<Long[]> affiliationIds;
    public LiveData<String[]> affiliationNames;
    public LiveData<Long[]> teamIds;
    public LiveData<String[]> teamNames;
    public LiveData<Team> defaultTeamLive;
    public LiveData<String> affStoreLink;
    public MutableLiveData<Boolean> updateSuccess = new MutableLiveData<>(false);
    private final FirebaseUser firebaseUser;
    private int ageSel = 0;
    private String genderSel = null;
    private float heightSel = 0;
    private float weightSel = 0;
    private long teamSel = 0L;
    private long affSel = 0L;
    private int physicalActivitySel = 0;
    public MutableLiveData<PhysicalActivity.Grade> physicalActGrade = new MutableLiveData<>(null);
    public boolean perDaySel = true;
    public String lastFanPicture = null;

    public MainProfileViewModel(@NonNull Application application) {
        super(application);
        repository = new UserProfileStorage(application);
        profileRepository = new ProfileRepository(application);
        userLive = repository.userLive;
        fanPicture = repository.fanPicture;
        fanDataLive = repository.fanDataLive;
        updateSuccess = profileRepository.result;
        affiliationIds = profileRepository.affIds;
        affiliationNames = profileRepository.affNames;
        teamIds = profileRepository.teamIds;
        teamNames = profileRepository.teamNames;
        affStoreLink = profileRepository.affStoreLink;
        defaultTeamLive = repository.getDefaultTeam();
        profileRepository.getAllTeams();
        profileRepository.getAllAffiliations();
        profileRepository.getAllUserMasters();
        firebaseUser = FirebaseAuthService.currentUser();
        if (firebaseUser != null) {
            Log.d(TAG, "Firebase " + firebaseUser.getProviderId());
        }
        profileLive = Transformations.map(userLive, new Function<User, Profile>() {
            @Override
            public Profile apply(User input) {
                Profile profile = repository.getProfile(input);
                long affId = profile.getUserPref().getAffiliationId();
                profileRepository.getAffiliationsForId(affId);
                return profile;
            }
        });
    }

    public String getDisplayName() {
        if (firebaseUser != null) {
            return firebaseUser.getDisplayName();
        }
        return null;
    }

    public String getEmail() {
        if (firebaseUser != null) {
            return firebaseUser.getEmail();
        }
        return null;
    }

    public String getMobile() {
        if (firebaseUser != null) {
            return firebaseUser.getPhoneNumber();
        }
        return null;
    }

    public void setAgeSel(int ageSel) {
        this.ageSel = ageSel;
    }

    public void setGenderSel(String genderSel) {
        this.genderSel = genderSel;
    }

    public void setHeightSel(float heightSel) {
        this.heightSel = heightSel;
    }

    public void setWeightSel(float weightSel) {
        this.weightSel = weightSel;
    }

    public void setTeamSel(long teamSel) {
        this.teamSel = teamSel;
    }

    public void setAffiliationSel(long affSel) {
        this.affSel = affSel;
    }

    public void setPhysicalActivitySel(int physicalActivitySel) {
        this.physicalActivitySel = physicalActivitySel;
    }

    public void setPerDaySel(boolean perDaySel) {
        this.perDaySel = perDaySel;
    }

    public long getAffFromPref() {
        if (profileLive.getValue() != null) {
            UserPref pref = profileLive.getValue().getUserPref();
            return pref.getAffiliationId();
        }
        return 0;
    }

    public void saveProfilePic(@NonNull String filePath) {
        Log.d(TAG, "Path " + filePath);
        if (userLive.getValue() == null) return;
        User user = userLive.getValue();
        lastFanPicture = filePath;
        repository.updateProfilePic(Uri.fromFile(new File(filePath)).toString());
        profileRepository.postUserProfile(user, filePath);
    }

    private void updateProfile(@NonNull Profile profile) {
        repository.updateProfile(profile);
    }

    public void updateUserProfile(@NonNull Profile profile) {

        if (profile.getUser() == null) {
            Log.d(TAG, "updateUserProfile user is null");
            return;
        }
        User user = profile.getUser();
        if (ageSel > 0) {
            user.setAge(ageSel);
        }
        if (genderSel != null && !genderSel.isEmpty()) {
            user.setGender(genderSel);
        } else {
            user.setGender("Male");
        }
        if (heightSel > 0) {
            user.setHeight(String.valueOf(heightSel));
        } else {
            user.setHeight(String.valueOf(HEIGHT_CM_DEFAULT));
        }
        if (weightSel > 0) {
            user.setWeight(String.valueOf(weightSel));
        } else {
            user.setWeight(String.valueOf(WEIGHT_KG_DEFAULT));
        }
        profile.setUser(user);
        updateProfile(profile);
        profileRepository.postUserProfile(user, null);
    }

    public void updateHealth(@NonNull Profile profile) {
        updateProfile(profile);
        profileRepository.postHealthHabitMedical(profile);
    }

    public void updateGoal(@NonNull Profile profile, @Nullable Set<Integer> selected, int hours, int minutes) {
        PhysicalActivity pa = null;

        Set<String> value = new HashSet<>();
        for (NameLogo item : getAllPhysicalActivities()) {
            if (selected.contains(item.getId())) {
                pa = new PhysicalActivity(item.getId(), item.getName());
                if (item.getId() == physicalActivitySel) {
                    if (perDaySel) {
                        pa.setPerDayHour(hours);
                        pa.setPerDayMin(minutes);
                        pa.setPerWeekHour(0);
                        pa.setPerWeekMin(0);
                    } else {
                        pa.setPerDayHour(0);
                        pa.setPerDayMin(0);
                        pa.setPerWeekHour(hours);
                        pa.setPerWeekMin(minutes);
                    }
                    physicalActGrade.postValue(pa.getGrade());
                } else {
                    pa.setPerDayHour(0);
                    pa.setPerDayMin(0);
                    pa.setPerWeekHour(0);
                    pa.setPerWeekMin(0);
                }
                value.add(pa.getStringValueForSelected());
            }
        }

        profile.setPhysicalActivities(value);

        updateProfile(profile);
        profileRepository.postPhysicalActivityGoal(profile);
    }

    public void updateUserPref(@NonNull UserPref pref, @NonNull Set<Integer> selectedSports) {
        if (profileLive.getValue() != null) {
            Profile profile = profileLive.getValue();
            pref.setTeamPrefId(teamSel);
            pref.setAffiliationId(affSel);
            Set<String> selectedSportsStrSet = repository.getStringSet(selectedSports);
            if (selectedSportsStrSet != null)
                pref.setSportsIds(selectedSportsStrSet);
            profile.setUserPref(pref);
            updateProfile(profile);
            if (teamIds.getValue() != null && teamNames.getValue() != null) {
                int index = Arrays.binarySearch(teamIds.getValue(), teamSel);
                String teamName = teamNames.getValue()[index];
                repository.setDefaultTeam(teamSel, teamName);
                logEvents("teamSelectedEditProfile4", "EditProfile4Fragment");
            }
            profileRepository.postFavSportTeam(profile);
        }
    }

    @NonNull
    public List<NameLogo> getAllSports() {
        return repository.getAllSports();
    }

    @NonNull
    public List<NameLogo> getAllPhysicalActivities() {
        return repository.getAllPhysicalActivities();
    }

    @NonNull
    public List<NameLogo> getAllHealthIssues() {
        return repository.getAllHealthIssues();
    }

    @NonNull
    public List<NameLogo> getAllHabits() {
        return repository.getAllHabits();
    }

    @Nullable
    public Set<Integer> getSelectedSports() {
        if (profileLive.getValue() != null) {
            return repository.getIntegerSet(profileLive.getValue().getUserPref().getSportsIds());
        }
        return null;
    }

    @Nullable
    public Set<Integer> getSelectedPhysicalActivities() {
        if (profileLive.getValue() != null) {
            Set<Integer> integerSet = new HashSet<>();
            List<PhysicalActivity> psList = profileLive.getValue().getPhysicalActivities();
            if (psList.size() == 0) return null;
            for(PhysicalActivity pa : psList) integerSet.add(pa.getPid());
            return integerSet;
        }
        return null;
    }

    @Nullable
    public PhysicalActivity getSelectedPhysicalActivity(int pid) {
        if (profileLive.getValue() != null) {
            List<PhysicalActivity> psList = profileLive.getValue().getPhysicalActivities();
            if (psList.size() == 0) return null;
            for(PhysicalActivity pa : psList) {
                if (pa.getPid() == pid) {
                    return pa;
                }
            }
        }
        return null;
    }

    public void updateHealth(int healthId) {
        if (profileLive.getValue() != null) {
            Profile profile = profileLive.getValue();
            Medical medical = profile.getMedical();
            HashSet<String> healthSet = medical.getHealthIssues();
            String temp = String.valueOf(healthId);
            if (healthSet.contains(temp)) {
                healthSet.remove(temp);
            } else {
                healthSet.add(temp);
            }
            medical.setHealthIssues(healthSet);
            profile.setMedical(medical);
            repository.updateProfile(profile);
        }
    }

    public void updateHabit(int healthId) {
        if (profileLive.getValue() != null) {
            Profile profile = profileLive.getValue();
            Medical medical = profile.getMedical();
            HashSet<String> habitSet = medical.getHabits();
            String temp = String.valueOf(healthId);
            if (habitSet.contains(temp)) {
                habitSet.remove(temp);
            } else {
                habitSet.add(temp);
            }
            medical.setHabits(habitSet);
            profile.setMedical(medical);
            repository.updateProfile(profile);
        }
    }

    public void logEvents(String itemName, String className) {
        AnalyticsService.INSTANCE.logClickEvents(itemName, className);
    }
}