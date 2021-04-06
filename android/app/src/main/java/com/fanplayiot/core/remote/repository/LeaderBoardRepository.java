package com.fanplayiot.core.remote.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.fanplayiot.core.db.local.FanplayiotDatabase;
import com.fanplayiot.core.db.local.FanplayiotTemp;
import com.fanplayiot.core.db.local.dao.HomeTempDao;
import com.fanplayiot.core.db.local.entity.LeaderBoard;
import com.fanplayiot.core.db.local.entity.User;
import com.fanplayiot.core.db.local.repository.HomeRepository;
import com.fanplayiot.core.remote.VolleySingleton;
import com.fanplayiot.core.remote.pojo.LeaderBoardArray;
import com.fanplayiot.core.utils.Constant;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LeaderBoardRepository {

    private static final String TAG = "LeaderBoardRepository";
    private static final String LB_TEAM_QUERY = "?teamId=";
    private static final String LB_SID_QUERY = "&sid=";
    private static final String LB_GLOBAL_SID_QUERY = "?sid=";
    private static final String LB_COUNT_QUERY = "&count=3";
    private static final String LB_DURATION_QUERY = "&duration=86400";
    //private static final String LB_DURATION_QUERY = "&duration=432000";
    private static final int VOLLEY_TIMEOUT_MS = 30000;
    private Context context;
    private HomeRepository homeRepository;
    //private HomeDao homeDao;
    private HomeTempDao dao;
    public LiveData<LeaderBoard[]> leaderBoardLive;
    public LiveData<User> userLive;

    public LeaderBoardRepository(Context context, final HomeRepository homeRepository) {
        this.context = context;
        this.homeRepository = homeRepository;
        dao = this.homeRepository.getTempDao();
        userLive = homeRepository.getUserLive();
        leaderBoardLive = dao.getAllLeaderBoardLive();
    }

    //curl -X GET "https://fanplaygurudevapi.azurewebsites.net/api/LeaderBoard/GetMobileLeaderboardWithUser?teamId=1&sid=3&count=10&duration=86400" -H "accept: text/plain"
    public void getLeaderBoard() {
        FanplayiotDatabase.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (homeRepository.getUserData() == null) return;
                if (homeRepository.getUserData().getSid() == null) return;
                if (homeRepository.getUserTeam() == null) return;
                if (homeRepository.getUserTeam().getTeamIdServer() == null) return;
                final long sid = homeRepository.getUserData().getSid();
                final long teamId = homeRepository.getUserTeam().getTeamIdServer();
                String getUrl = Constant.BASEURL + Constant.GET_LEADERBOARD + LB_TEAM_QUERY + teamId + LB_SID_QUERY + sid + LB_COUNT_QUERY + LB_DURATION_QUERY;
                //Log.d(TAG, tokenId);
                //Log.d(TAG, getUrl);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, getUrl, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    Log.d(TAG, response.toString());
                                    LeaderBoardArray boardArray = LeaderBoardArray.getInstance(LeaderBoardArray.class, response.toString());
                                    if (boardArray == null) {
                                        Log.d(TAG, "No data returned fe");
                                        return;
                                    }
                                    // 1 is Global, 2 is FanEngage, 3 is FanFit
                                    LeaderBoard[] input = boardArray.getLeaderBoardsForType(2);
                                    refresh(Constant.GET_LEADERBOARD, input);
                                    //leaderBoardLive.postValue(boardArray);
                                } catch (JSONException | IllegalAccessException | InstantiationException e) {
                                    Log.e(TAG, "error ", e);
                                } catch (Exception ex) {
                                    Log.e(TAG, "other error ", ex);
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "error " + error.getMessage(), error);
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
                jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                        VOLLEY_TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                VolleySingleton.getInstance(context).
                        addJSONRequestToQueue(jsonObjectRequest);

            }
        });
    }

    private void refresh(String api, final LeaderBoard[] input) {
        FanplayiotTemp.tempWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (Constant.GET_LEADERBOARD.equals(api))
                    dao.refresh(input);
                else if (Constant.GET_FITNESS_LEADERBOARD.equals(api)) {
                    dao.refresh(input);
                } else if (Constant.GET_GLOBAL_LEADERBOARD.equals(api)) {
                    dao.refresh(input);
                }
            }
        });
    }

    public void resetAllLeaderBoard() {
        FanplayiotTemp.tempWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                dao.resetAllLeaderBoard();

                getGlobalLeaderBoard();
                getLeaderBoard();
                getFitnessLeaderBoard();
            }
        });
    }

    //curl -X GET "https://fanplaygurudevapi.azurewebsites.net/api/LeaderBoard/GetGlobalLeaderboard?sid=81&count=3" -H "accept: text/plain"
    public void getGlobalLeaderBoard() {
        FanplayiotDatabase.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (homeRepository.getUserData() == null) return;
                User user = homeRepository.getUserData();
                final String tokenId = user.getTokenId();
                String sid = String.valueOf(user.getSid());

                String getUrl = Constant.BASEURL + Constant.GET_GLOBAL_LEADERBOARD + LB_GLOBAL_SID_QUERY + sid + LB_COUNT_QUERY;
                //Log.d(TAG, tokenId + " sid " + sid);
                //Log.d(TAG, getUrl);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, getUrl, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    Log.d(TAG, response.toString());
                                    LeaderBoardArray boardArray = LeaderBoardArray.getInstance(LeaderBoardArray.class, response.toString());
                                    if (boardArray != null && boardArray.getLeaderBoards() != null
                                            && boardArray.getLeaderBoards().length > 0 ) {
                                        refresh(Constant.GET_GLOBAL_LEADERBOARD, boardArray.getLeaderBoardsForType(1));
                                    }
                                    //Log.e(TAG, "No data returned");
                                } catch (JSONException | IllegalAccessException | InstantiationException e) {
                                    Log.e(TAG, "error ", e);
                                } catch (Exception ex) {
                                    Log.e(TAG, "other error ", ex);
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "error " + error.getMessage(), error);
                    }
                }) {
                    @SuppressWarnings("RedundantThrows")
                    @NonNull
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<>();
                        String authValue = "Bearer " + tokenId;
                        headers.put("Authorization", authValue);
                        //headers.put("Content-Type", "application/json; charset=utf-8");
                        headers.put("accept", "text/plain");
                        return headers;
                    }
                };
                jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                        VOLLEY_TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                VolleySingleton.getInstance(context).
                        addJSONRequestToQueue(jsonObjectRequest);

            }
        });
    }

    //curl -X GET "https://fanplaygurudevapi.azurewebsites.net/api/LeaderBoard/GetFitnessLeaderboard?teamId=1&sid=81&count=3&duration=864600" -H "accept: text/plain"
    public void getFitnessLeaderBoard() {
        FanplayiotDatabase.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (homeRepository.getUserData() == null) return;
                if (homeRepository.getUserTeam() == null) return;
                if (homeRepository.getUserTeam().getTeamIdServer() == null) return;
                User user = homeRepository.getUserData();
                final String tokenId = user.getTokenId();
                String sid = String.valueOf(user.getSid());
                final long teamId = homeRepository.getUserTeam().getTeamIdServer();

                String getUrl = Constant.BASEURL + Constant.GET_FITNESS_LEADERBOARD + LB_TEAM_QUERY + teamId + LB_SID_QUERY + sid + LB_COUNT_QUERY + LB_DURATION_QUERY;
                //Log.d(TAG, tokenId + " sid " + sid);
                //Log.d(TAG, getUrl);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, getUrl, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    Log.d(TAG, response.toString());
                                    LeaderBoardArray boardArray = LeaderBoardArray.getInstance(LeaderBoardArray.class, response.toString());
                                    if (boardArray != null && boardArray.getLeaderBoards() != null
                                            && boardArray.getLeaderBoards().length > 0 ) {
                                        refresh(Constant.GET_FITNESS_LEADERBOARD, boardArray.getLeaderBoardsForType(3));
                                    }


                                    //Log.e(TAG, "No data returned");
                                } catch (JSONException | IllegalAccessException | InstantiationException e) {
                                    Log.e(TAG, "error ", e);
                                } catch (Exception ex) {
                                    Log.e(TAG, "other error ", ex);
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "error " + error.getMessage(), error);
                    }
                }) {
                    @SuppressWarnings("RedundantThrows")
                    @NonNull
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<>();
                        String authValue = "Bearer " + tokenId;
                        headers.put("Authorization", authValue);
                        //headers.put("Content-Type", "application/json; charset=utf-8");
                        headers.put("accept", "text/plain");
                        return headers;
                    }
                };
                jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                        VOLLEY_TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                VolleySingleton.getInstance(context).
                        addJSONRequestToQueue(jsonObjectRequest);

            }
        });
    }
}
