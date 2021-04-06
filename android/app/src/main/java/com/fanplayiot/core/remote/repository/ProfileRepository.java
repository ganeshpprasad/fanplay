package com.fanplayiot.core.remote.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.fanplayiot.core.db.local.entity.Profile;
import com.fanplayiot.core.db.local.entity.User;
import com.fanplayiot.core.db.local.repository.UserProfileStorage;
import com.fanplayiot.core.remote.ApiCallMultipartRequest;
import com.fanplayiot.core.remote.VolleySingleton;
import com.fanplayiot.core.remote.pojo.Affiliation;
import com.fanplayiot.core.remote.pojo.Affiliations;
import com.fanplayiot.core.remote.pojo.AllTeams;
import com.fanplayiot.core.remote.pojo.MedicalProfile;
import com.fanplayiot.core.remote.pojo.PhysicalGoal;
import com.fanplayiot.core.remote.pojo.SportsTeam;
import com.fanplayiot.core.remote.pojo.UpdateProfile;
import com.fanplayiot.core.utils.Constant;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.fanplayiot.core.utils.Constant.AFFILIATION_KEY;
import static com.fanplayiot.core.utils.Constant.PREF_FILE_KEY;


public class ProfileRepository {
    private static final String TAG = "ProfileRepository";
    private Context context;
    public MutableLiveData<Boolean> result = new MutableLiveData<>(false);
    public MutableLiveData<Long[]> affIds = new MutableLiveData<>(null);
    public MutableLiveData<String[]> affNames = new MutableLiveData<>(null);
    public MutableLiveData<Long[]> teamIds = new MutableLiveData<>(null);
    public MutableLiveData<String[]> teamNames = new MutableLiveData<>(null);
    public MutableLiveData<String> affStoreLink = new MutableLiveData<>(null);
    private static final int VOLLEY_TIMEOUT_MS = 30000;
    private static final String AFF_ID_PARAM = "?id=";

    public ProfileRepository(Context context) {
        this.context = context;
    }

    public void postUserProfile(@NonNull final User user, @Nullable final String imgPath) {
        result.postValue(false);

        SharedPreferences sharedPref = context.getSharedPreferences(PREF_FILE_KEY, Context.MODE_PRIVATE);
        long affId = sharedPref.getLong(AFFILIATION_KEY, 0L);
        UpdateProfile userProfile = new UpdateProfile();
        String[] filePath = null;
        userProfile.setUser(user);
        if (imgPath != null && !imgPath.isEmpty()) {
            userProfile.setImagePath(imgPath);
            filePath = new String[1];
            filePath[0] = imgPath;
            Log.d(TAG, "Image path " + imgPath);
        }
        userProfile.setAffiliationId((int) affId);
        final String urlWithParam = userProfile.getURLWithParams();
        final String tokenId = user.getTokenId();
        //Log.d(TAG, "Token " + tokenId);
        if (urlWithParam == null) {
            Log.d(TAG, "Sid is null ");
            return;
        }
        Log.d(TAG, urlWithParam);
        //"?sid=" + user.getSid() + "&gender=Male&file=null"
        final ApiCallMultipartRequest request = new ApiCallMultipartRequest(urlWithParam,
                filePath,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        int code = response.statusCode;
                        Log.d(TAG, "Status code " + code);
                        if (code == 200) {
                            result.postValue(true);
                        } else {
                            result.postValue(false);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "error " + error.getMessage(), error);
                VolleySingleton.getInstance(context).handleError(error);
            }
        }) {
            @SuppressWarnings("RedundantThrows")
            @NonNull
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String authValue = "Bearer " + tokenId;
                headers.put("Authorization", authValue);
                headers.put("accept", "text/plain");
                return headers;
            }

        };
        request.setRetryPolicy(new DefaultRetryPolicy(
                VOLLEY_TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleySingleton.getInstance(context).addApiCallFileToQueue(request);
    }


    @WorkerThread
    public void getAllTeams() {
        JsonObjectRequest api = new JsonObjectRequest(Request.Method.GET, Constant.BASEURL + Constant.GET_ALLTEAMS,
                null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d(TAG, response.toString());
                            AllTeams allTeams = AllTeams.getInstance(AllTeams.class, response.toString());
                            if (allTeams != null) {
                                if (allTeams.getIds() != null && allTeams.getTeamNames() != null) {
                                    teamIds.postValue(allTeams.getIds());
                                    teamNames.postValue(allTeams.getTeamNames());
                                }
                            }
                        } catch (IllegalAccessException iae) {
                            Log.e(TAG, "error ", iae);
                        } catch (InstantiationException ie) {
                            Log.e(TAG, "InstantiationException ", ie);
                        } catch (JSONException e) {
                            Log.e(TAG, "JSONException ", e);
                        } catch (Exception e) {
                            Log.e(TAG, "other error ", e);
                        }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "error ", error);
            }
        }) {
            @NonNull
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("accept", "text/plain");
                return headers;
            }
        };
        VolleySingleton.getInstance(context).addJSONRequestToQueue(api);
    }

    public void getAllAffiliations() {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                Constant.BASEURL + Constant.GET_ALL_AFFILIATIONS , null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                        try {
                            Affiliations affiliations = Affiliations.getInstance(Affiliations.class, response.toString());
                            if (affiliations == null) {
                                Log.d(TAG, "affiliations is null");
                                return;
                            }
                            if (affiliations.getIds() != null && affiliations.getAffNames() != null) {
                                affIds.postValue(affiliations.getIds());
                                affNames.postValue(affiliations.getAffNames());
                            }
                        } catch (IllegalAccessException | InstantiationException | JSONException e) {
                            Log.e(TAG, "JSONException ", e);
                        } catch (Exception e) {
                            Log.e(TAG, "other error ", e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Volley Error: " + error.toString());
            }
        }) {
            @SuppressWarnings("RedundantThrows")
            @NonNull
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("accept", "text/plain");
                return headers;
            }
        };
        VolleySingleton.getInstance(context).addJSONRequestToQueue(jsonObjectRequest);
    }


    public void getAffiliationsForId(long affiliationId) {
        if (affiliationId <= 0) {
            affStoreLink.postValue(null);
            return;
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                Constant.BASEURL + Constant.GET_ALL_AFFILIATIONS + AFF_ID_PARAM + affiliationId , null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                        try {
                            Affiliation affiliation = Affiliation.getInstance(Affiliation.class, response.toString());
                            if (affiliation == null || affiliation.getStorelink() == null ||
                                    affiliation.getStorelink().equals("") ||
                                    affiliation.getStorelink().equals("null")) {
                                Log.d(TAG, "affiliations is null");
                                affStoreLink.postValue(null);
                                return;
                            }
                            Log.d(TAG, "store link " + affiliation.getStorelink());
                            affStoreLink.postValue(affiliation.getStorelink());
                        } catch (IllegalAccessException | InstantiationException | JSONException e) {
                            Log.e(TAG, "JSONException ", e);
                        } catch (Exception e) {
                            Log.e(TAG, "other error ", e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Volley Error: " + error.toString());
            }
        }) {
            @SuppressWarnings("RedundantThrows")
            @NonNull
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("accept", "text/plain");
                return headers;
            }
        };
        VolleySingleton.getInstance(context).addJSONRequestToQueue(jsonObjectRequest);
    }

    public void getAllUserMasters() {
        final UserProfileStorage storage = new UserProfileStorage(context);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                Constant.BASEURL + Constant.GET_ALL_USERMASTERS , null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                        try {
                            JSONObject masters = response.getJSONObject("response").getJSONObject("userprofilemasters");
                            if (!masters.toString().isEmpty()) storage.updateMasters(masters.toString());
                        } catch (JSONException e) {
                            Log.e(TAG, "JSONException ", e);
                        } catch (Exception e) {
                            Log.e(TAG, "other error ", e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Volley Error: " + error.toString());
            }
        }) {
            @SuppressWarnings("RedundantThrows")
            @NonNull
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("accept", "text/plain");
                return headers;
            }
        };
        VolleySingleton.getInstance(context).addJSONRequestToQueue(jsonObjectRequest);
    }

    public void postHealthHabitMedical(@NonNull final Profile profile) {
        result.postValue(false);
        MedicalProfile medicalProfile = new MedicalProfile();
        medicalProfile.setProfile(profile);
        if (profile.getUser() == null) {
            Log.d(TAG, "user in profile is null");
            return;
        }
        if (profile.getUser().getSid() == null) {
            Log.d(TAG, "user sid is null");
            return;
        }
        final String tokenId = profile.getUser().getTokenId();
        JSONObject medicalProfileJson = medicalProfile.getJSONObject();
        if (medicalProfileJson == null) return;
        Log.d(TAG, medicalProfileJson.toString());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                Constant.BASEURL + Constant.POST_USER_MEDICAL, medicalProfileJson,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                        try {
                            int code = response.getInt("statuscode");
                            Log.d(TAG, "Status code " + code);
                            result.postValue(code == 200);
                        } catch (JSONException je) {
                            Log.d(TAG, "json error");
                        } catch (Exception e) {
                            Log.e(TAG, "error", e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "error " + error.getMessage(), error);
                VolleySingleton.getInstance(context).handleError(error);
            }
        }) {
            @SuppressWarnings("RedundantThrows")
            @NonNull
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String authValue = "Bearer " + tokenId;
                headers.put("Authorization", authValue);
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };
        VolleySingleton.getInstance(context).addJSONRequestToQueue(jsonObjectRequest);
    }

    public void postPhysicalActivityGoal(@NonNull final Profile profile) {
        result.postValue(false);
        PhysicalGoal physicalGoal = new PhysicalGoal();
        physicalGoal.setProfile(profile);
        if (profile.getUser() == null) {
            Log.d(TAG, "user in profile is null");
            return;
        }
        if (profile.getUser().getSid() == null) {
            Log.d(TAG, "user sid is null");
            return;
        }
        final String tokenId = profile.getUser().getTokenId();
        JSONObject phyGoalJson = physicalGoal.getJSONObject();
        if (phyGoalJson == null) return;
        Log.d(TAG, phyGoalJson.toString());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                Constant.BASEURL + Constant.POST_USER_PHYSICAL_GOAL, phyGoalJson,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                        try {
                            int code = response.getInt("statuscode");
                            Log.d(TAG, "Status code " + code);
                            result.postValue(code == 200);
                        } catch (JSONException je) {
                            Log.d(TAG, "json error");
                        } catch (Exception e) {
                            Log.e(TAG, "error", e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "error " + error.getMessage(), error);
                VolleySingleton.getInstance(context).handleError(error);
            }
        }) {
            @SuppressWarnings("RedundantThrows")
            @NonNull
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String authValue = "Bearer " + tokenId;
                headers.put("Authorization", authValue);
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };
        VolleySingleton.getInstance(context).addJSONRequestToQueue(jsonObjectRequest);
    }

    public void postFavSportTeam(@NonNull final Profile profile) {
        result.postValue(false);
        SportsTeam sportsTeam = new SportsTeam();
        sportsTeam.setProfile(profile);
        if (profile.getUser() == null) {
            Log.d(TAG, "user in profile is null");
            return;
        }
        if (profile.getUser().getSid() == null) {
            Log.d(TAG, "user sid is null");
            return;
        }
        final String tokenId = profile.getUser().getTokenId();
        JSONObject sportJson = sportsTeam.getJSONObject();
        if (sportJson == null) return;
        Log.d(TAG, sportJson.toString());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                Constant.BASEURL + Constant.POST_USER_SPORTS, sportJson,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                        try {
                            int code = response.getInt("statuscode");
                            Log.d(TAG, "Status code " + code);
                            result.postValue(code == 200);
                        } catch (JSONException je) {
                            Log.d(TAG, "json error");
                        } catch (Exception e) {
                            Log.e(TAG, "error", e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "error " + error.getMessage(), error);
                VolleySingleton.getInstance(context).handleError(error);
            }
        }) {
            @SuppressWarnings("RedundantThrows")
            @NonNull
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String authValue = "Bearer " + tokenId;
                headers.put("Authorization", authValue);
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };
        VolleySingleton.getInstance(context).addJSONRequestToQueue(jsonObjectRequest);
    }
}
