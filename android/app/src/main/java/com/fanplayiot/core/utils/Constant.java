package com.fanplayiot.core.utils;

import com.demoproject.BuildConfig;

public class Constant {


    //Broadcast notice
    public static final String FOUND_DEVICE = "com.mcube.ms.sdk.demo.found_device";    //Scan/discover devices
    public static final String DEVICE_RSSI = "com.mcube.ms.sdk.demo.device_rssi";      //Signal strength of device
    public static final String DEVICE_CONNECT_STATE = "com.mcube.sdk.demo.device_state";       //Set connection status
    public static final String DEVICE_FIRMWARE = "com.mcube.sdk.demo.device_firmware";  // Device in firmware version
    public static final String DEVICE_BATTERY = "com.mcube.sdk.demo.device_battery";    //Device Battery Status and charge
    public static final String DEVICE_MOTION_CHANGE = "com.mcubee.sdk.demo.devic_motion"; // Device Status
    public static final String SELFIE = "com.mcube.sdk.demo.selfie"; //Selfie
    public static final String SEDENTARYCHANGED = "com.mcube.sdk.demo.sedentary"; //Sedentary
    public static final String HEART_RATE = "com.mcube.sdk.demo.heart_rate"; //HR
    public static final String BLOOD_PRESSURE = "com.mcube.sdk.demo.blood_pressure";  //BP
    public static final String SYNC_HISTORY = "com.mcube.sdk.demo.sync_history";
    public static final String SYNC_STATE = "com.mcube.sdk.demo.sync_state";
    public static final String SYNC_HR = "com.mcube.sdk.demo.sync_hr";
    public static final String SYNC_BP = "com.mcube.sdk.demo.sync_bp";
    public static final String VAST_ALARM_NAME_SET = "com.mcube.sdk.demo.vast_alarm_name_set";
    public static final String VAST_ALARM_TIME_SET = "com.mcube.sdk.demo.vast_alarm_time_set";
    public static final String OTA_CHECK = "com.mcube.sdk.demo.ota_check";
    public static final String WERUN = "com.mcube.sdk.demo.werun";
    public static final String BLOOD_OXYGEN = "com.mcube.sdk.demo.blood_oxygen";
    public static final String MEDICINE_ALARM = "com.mcube.sdk.demo.medicine_alarm";


    //Broadcast parameter passing

    public final static String device_name = "device_name";
    public final static String device_address = "device_address";
    public final static String device_rssi = "device_rssi";
    public final static String connected = "connected";
    public final static String firmware_version = "firmware_version";
    public final static String pair = "pair";
    public final static String hrBP = "hrBP";
    public final static String battery = "battery";
    public final static String battery_state = "battery_state";
    public final static String device_motion_state = "device_motion_state";
    public final static String device_steps = "device_steps";
    public final static String rate = "rate";
    public final static String systolic = "systolic";
    public final static String diastolic = "diastolic";

    public final static String sync_address = "address";
    public final static String sync_state = "sync_state";
    public final static String steps = "steps";
    public final static String sync_start = "sync_start";
    public final static String sync_far = "sync_far";
    public final static String sync_end = "sync_end";
    public final static String sync_time = "sync_time";
    public final static String success = "success";
    public final static String upgrade = "upgrade";
    public final static String downloaded = "ota_downloaded";
    public final static String ota_start = "ota_start";
    public final static String ota_process = "ota_process";
    public final static String ota_end = "ota_end";
    public final static String werun_url = "werun_url";
    public final static String blood_oxygen = "blood_oxygen";

    // App Specific constants
    public static final String BASEURL = BuildConfig.BASE_URL;
    //public static final String BASEURL = "https://fanplaygurudevapi.azurewebsites.net/api/";

    //public static final String BASEURL = "http://fanplaydashboard.westus.cloudapp.azure.com:8345/api/";
    public static final String LOGIN = "Signup/CheckSignup";
    public static final String SIGN_UP = "Signup/AddSignupData";
    public static final String POST_BP = "Insights/AddBP";
    public static final String POST_HR = "Insights/AddHR";
    public static final String POST_SCD = "Insights/AddSCD";
    public static final String POST_FANMETRIC = "MobileUser/DisplayFanmetricBySId";
    public static final String POST_SESSIONDATA = "MobileUser/SessionDetails";
    public static final String POST_FANENGAGEMENT = "FanEngagement/CreateFanEngagement";
    public static final String POST_SIGNIN = "Login/ValidateSignIn";
    public static final String GET_ALLTEAMS = "Master/GetAllTeams";
    public static final String GET_TEAM = "TeamDetails/GetTeamPlayersData";
    public static final String GET_SPONSORS = "Sponsor/GetSponsorsData";
    public static final String GET_FANEMOTE = "FanEngagement/GetFanEmote";
    public static final String GET_LEADERBOARD = "LeaderBoard/GetMobileLeaderboardWithUser";
    public static final String GET_GLOBAL_LEADERBOARD = "LeaderBoard/GetGlobalLeaderboard";
    public static final String GET_FITNESS_LEADERBOARD = "LeaderBoard/GetFitnessLeaderboard";
    public static final String GET_ALL_AFFILIATIONS = "Master/GetAllAffiliations";
    public static final String POST_USERPROFILE = "User/UpdateUserProfiles";
    public static final String GET_ALLUSERDETAILS = "User/GetAllUserDetailsByIdToken";
    public static final String POST_SPONSORANALYTICS = "SponsorAnalytics/CreateSponsorAnalytics";
    public static final String POST_SYNC_FANENGAGEMENT = "FanEngagement/SyncFanEngagement";
    public static final String POST_USER_MEDICAL = "User/UpdateUserHabitHealthMedical";
    public static final String POST_USER_PHYSICAL_GOAL = "User/UpdateUserPhysicalActivitiesGoal";
    public static final String POST_USER_SPORTS = "User/UpdateUserFavouriteSportsTeamAffiliation";
    public static final String GET_ALL_USERMASTERS = "Master/GetAllUserMastersDetails";
    public static final String POST_FAN_FITNESS = "Fitness/CreateUserFitness";
    public static final String GET_SCD_FITNESS = "Fitness/GetStepsCaloriesDistance";
    public static final String GET_HR_FITNESS = "Fitness/GetHeartRate";
    public static final String GET_BP_FITNESS = "Fitness/GetBloodPressure";
    public static final String GET_FANFIT_SCORES = "Fitness/GetUserFanFitPointsAndScoresDetails";// "Fitness/GetFanFitScores";
    public static final String GET_FE_DETAILS = "Dashboard/GetFEDetailsByTeamId";
    public static final String GET_RNR_POINTS_STATUS = "PointsMaster/GetRnRPointStatus";
    public static final String POST_RNR_POINTS = "PointsMaster/CreateRnRPoint";
    public static final String POST_REGISTER_FCM = "NotificationMessage/Regsiter";
    public static final String GET_FE_DETAILS_BY_TEAMS = "Dashboard/GetFEDetailsByTeamIds";
    public static final String POST_CHALLENGE = "FanSocial/CreateChallengeDetails";
    public static final String GET_CHALLENGE = "FanSocial/GetChallengeSessionDetailsByGivenId";
    public static final String GET_CHALLENGE_SESSION = "FanSocial/GetChallengeSessionDetailsBySessionId";
    public static final String GET_CREATE_RAZORPAYORDER = "FanSocial/CreateRazorpayOrder";
    public static final String POST_PAYMENT = "FanSocial/UpdateRazorpayPaymentDetails";

    // Fast Ble Name, UUIDs
    public static final String NAME = "FAN GURU";
    public static final String CONST_SERVICE_ID  = "00005500-d102-11e1-9b23-00025b00a5a5";
    public static final String CONST_CHARAC_ID   = "00005501-d102-11e1-9b23-00025b00a5a5";

    // Permission Request Code
    public static final int LOCATION_REQUEST_CODE = 1;
    public static final int CAMERA_REQUEST_CODE = 2;
    public static final int IMAGE_REQUEST_CODE = 3;
    public static final int REQUEST_STORAGE_CODE = 4;
    public static final int REQUEST_IMAGE_CAPTURE = 5;
    public static final int MOMENTS_IMAGE_REQUEST_CODE = 130002;
    public static final int APP_UPDATE_REQUEST_CODE = 10;
    public static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34; // Google fit permission
    public static final int PHONE_STATE_PERMISSION_REQUEST_CODE = 233; // Call notify permission
    public static final int SMS_PERMISSION_REQUEST_CODE = 234; // Sms notify permission
    public static final int OTHER_APP_NOTIFY_REQUEST_CODE = 235; // other app notify permission

    public static final String LOGIN_EXTRA = "com.fanplayiot.core.LOGIN_EXTRA";
    public static final String EXTRA_DISPLAY_NAME = "com.fanplayiot.core.EXTRA_DISPLAY_NAME";
    public static final String EXTRA_LOGIN_TYPE = "com.fanplayiot.core.EXTRA_LOGIN_TYPE";
    public static final String OTHER_APP_NOTIFY_ACTION = "com.fanplayiot.core.OTHER_APP_NOTIFY_ACTION";
    public static final String EXTRA_OTHER_APP_DEF_ID = "com.fanplayiot.core.EXTRA_OTHER_APP_DEF_ID";
    public static final String EXTRA_OTHER_APP_MESSAGE = "com.fanplayiot.core.EXTRA_OTHER_APP_MESSAGE";

    // Notification
    public static final String HubName = BuildConfig.hubName; //"fangurudev-hub1";
    // "Endpoint=sb://fangurudev-nh.servicebus.windows.net/;SharedAccessKeyName=DefaultListenSharedAccessSignature;SharedAccessKey=hZ3cTIXFOafdpk3B0zGbTpI2/x8w+nF0ciPhcF+D5p4=";
    public static final String HubListenConnectionString = BuildConfig.hubListenConnectionString;

    // Store Url
    // "https://www.flipkart.com/fanplay-iot-fanband/p/itmabeeda81cadb7" for csk
    // https://fanplayiot.com/ for others
    public static final String STORE_URL = "https://fanplayiot.com/";

    // Shared preference
    public static final String PREF_FILE_KEY = "com.fanplayiot.core.PREFERENCE_FILE_KEY";
    public static final String PREF_PROFILE = "profile"; // SDK Manager uses this shared preference file
    public static final String MASTER_UPDATED_KEY = "MASTER_UPDATED_KEY";
    public static final String AFFILIATION_KEY = "AFFILIATION_KEY";
    public static final String TEAM_PREF_KEY = "TEAM_PREF_KEY";
    public static final String FAV_SPORTS_KEY = "FAV_SPORTS_KEY";
    public static final String FOLLOW_FITNESS_KEY = "FOLLOW_FITNESS_KEY";
    public static final String CUSTOM_FITNESS_KEY = "CUSTOM_FITNESS_KEY";
    public static final String HEALTH_ISSUES_KEY = "HEALTH_ISSUES_KEY";
    public static final String HABITS_KEY = "HABITS_KEY";
    public static final String BLOOD_SUGAR_KEY = "BLOOD_SUGAR_KEY";
    public static final String BP_SYSTOLIC_KEY = "BP_SYSTOLIC_KEY";
    public static final String BP_DIASTOLIC_KEY = "BP_DIASTOLIC_KEY";
    public static final String HEART_RATE_KEY = "HEART_RATE_KEY";
    public static final String PHYSICAL_ACTIVITY_KEY = "PHYSICAL_ACTIVITY_KEY";
    public static final String GOAL_STEPS_KEY = "GOAL_STEPS_KEY";
    public static final String GOAL_CALORIES_KEY = "GOAL_CALORIES_KEY";
    public static final String GOAL_DISTANCE_KEY = "GOAL_DISTANCE_KEY";
    public static final String GOAL_SLEEP_KEY = "GOAL_SLEEP_KEY";
    public static final String ALL_MASTER_KEY = "ALL_MASTER_KEY";
    public static final String REFERRED_BY = "REFERRED_BY";
    public static final String SEDENTARY_KEY = "SEDENTARY_KEY";
    public static final String ALARM_KEY = "ALARM_KEY";
    public static final String NOTIFY_KEY = "NOTIFY_KEY";
    public static final String DISPLAY_SETTINGS_KEY = "DISPLAY_SETTINGS_KEY";
    public static final String FCM_TOKEN = "FCM_TOKEN";

    // Keys used in profile shared preference file used by SDK Manager
    public static final String STEPS_KEY = "steps";
    public static final String GENDER_KEY = "gender";
    public static final String HEIGHT_KEY = "height";
    public static final String WEIGHT_KEY = "weight";
    public static final String AGE_KEY = "age";
    public static final String POSTURE_KEY = "posture";

    // FanEngage
    public static final int COUNTDOWNINTERVAL = 1000; // Common count down interval 1 min in ms
}
