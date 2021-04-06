package com.fanplayiot.core.remote.repository

import android.content.Context
import android.util.Log
import androidx.annotation.WorkerThread
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.fanplayiot.core.db.local.entity.LoginType
import com.fanplayiot.core.db.local.entity.Team
import com.fanplayiot.core.db.local.entity.User
import com.fanplayiot.core.db.local.repository.HomeRepository
import com.fanplayiot.core.db.local.repository.UserProfileStorage
import com.fanplayiot.core.remote.VolleySingleton
import com.fanplayiot.core.remote.pojo.*
import com.fanplayiot.core.remote.pojo.BaseData.Companion.getInstance
import com.fanplayiot.core.utils.Constant
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class TeamRepository(
        private val context: Context, private val repository: HomeRepository) {
    //private static final String SID_QUERY = "?userId=";

    fun postValidateSignIn(tokenId: String, displayName: String?, user: User?) {
        val signInUser = SignInUser()
        signInUser.setDisplayName(displayName)
        signInUser.setUser(user)
        //Log.d(TAG, tokenId);
        Log.d(TAG, signInUser.getJSONObject().toString())
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(Method.POST,
                Constant.BASEURL + Constant.POST_SIGNIN, signInUser.getJSONObject(),
                Response.Listener { response ->
                    Log.d(TAG, response.toString())
                    try {
                        val jsonObject = JSONObject(response.toString())
                        val sid = jsonObject.getJSONObject("response").getLong("userSignUpId")
                        repository.updateUser(sid)
                    } catch (e: Exception) {
                        Log.e(TAG, "error in postValidateSignIn", e)
                    }
                }, Response.ErrorListener { error ->
            Log.e(TAG, "error in postValidateSignIn" + error.message, error)
            VolleySingleton.getInstance(context).handleError(error)
        }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers: MutableMap<String, String> = HashMap()
                val authValue = "Bearer $tokenId"
                headers["Authorization"] = authValue
                headers["Content-Type"] = "application/json; charset=utf-8"
                return headers
            }
        }
        VolleySingleton.getInstance(context).addJSONRequestToQueue(jsonObjectRequest)
    }

    @WorkerThread
    fun getAllTeamsInfo() {
            val api: JsonObjectRequest = object : JsonObjectRequest(Method.GET, Constant.BASEURL + Constant.GET_ALLTEAMS,
                    null,
                    Response.Listener { response ->
                        try {
                            val jsonStr = response.toString()
                            Log.d(TAG, jsonStr)
                            val teamsInfo: TeamsInfo? = getInstance<TeamsInfo>(TeamsInfo::class.java, jsonStr)
                            if (teamsInfo != null && teamsInfo.teams != null && teamsInfo.jsonString != null) {
                                repository.setTeamsList(jsonStr, teamsInfo.jsonString!!, teamsInfo.teams!!)
                            }
                        } catch (iae: IllegalAccessException) {
                            Log.e(TAG, "IllegalAccessException ", iae)
                        } catch (ie: InstantiationException) {
                            Log.e(TAG, "InstantiationException ", ie)
                        } catch (e: JSONException) {
                            Log.e(TAG, "JSONException ", e)
                        } catch (e: Exception) {
                            Log.e(TAG, "other error ", e)
                        }
                    }, Response.ErrorListener { error ->
                Log.e(TAG, "error in getAllTeamsInfo", error)
                VolleySingleton.getInstance(context).logError(error)
            }) {
                override fun getHeaders(): Map<String, String> {
                    val headers: MutableMap<String, String> = HashMap()
                    headers["accept"] = "text/plain"
                    return headers
                }
            }
            VolleySingleton.getInstance(context).addJSONRequestToQueue(api)
        }

    // curl -X GET "https://fanplaygurudevapi.azurewebsites.net/api/TeamDetails/GetTeamPlayersData?teamId=1" -H "accept: text/plain"
    @WorkerThread
    fun getTeamAndPlayers(team: Team?) {
        if (team == null || team.teamIdServer == null) return
        val teamId = team.teamIdServer!!
        //long teamId = 2;
        //Log.d(TAG, Constant.BASEURL + Constant.GET_TEAM + TEAM_QUERY + teamId);
        val api: JsonObjectRequest = object : JsonObjectRequest(Method.GET, Constant.BASEURL + Constant.GET_TEAM + TEAM_QUERY + teamId,
                null,
                Response.Listener { response ->
                    try {
                        Log.d(TAG, response.toString())
                        val players: Players? = getInstance<Players>(Players::class.java, response.toString())
                        if (players == null) {
                            Log.e(TAG, "players in response is null")
                            return@Listener
                        }
                        if (players.playerList != null && players.playerList.size > 0) {
                            repository.addPlayers(players.playerList, players.teamName, players.teamIdServer)
                        }
                    } catch (iae: IllegalAccessException) {
                        Log.e(TAG, "error ", iae)
                    } catch (ie: InstantiationException) {
                        Log.e(TAG, "InstantiationException ", ie)
                    } catch (e: JSONException) {
                        Log.e(TAG, "JSONException ", e)
                    } catch (e: Exception) {
                        Log.e(TAG, "other error ", e)
                    }
                }, Response.ErrorListener { error ->
            Log.e(TAG, "error in getTeamAndPlayers", error)
            VolleySingleton.getInstance(context).logError(error)
        }) {
            override fun getHeaders(): Map<String, String> {
                val headers: MutableMap<String, String> = HashMap()
                headers["accept"] = "text/plain"
                return headers
            }
        }
        VolleySingleton.getInstance(context).addJSONRequestToQueue(api)
    }

    // GET /api/User/GetAllUserDetailsByIdToken
    fun getRecentUserDetails(tokenId: String, displayName: String?, @LoginType type: Int) {
        //Log.d(TAG, "Getting user history");
        val storage = UserProfileStorage(context)
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(Method.GET,
                Constant.BASEURL + Constant.GET_ALLUSERDETAILS, null,
                Response.Listener { response ->
                    Log.d(TAG, response.toString())
                    try {
                        val userDetails: UserDetails? = getInstance<UserDetails>(UserDetails::class.java, response.toString())
                        if (userDetails != null && userDetails.user != null) {
                            // User history available in server so store that in db
                            if (displayName != null && !displayName.isEmpty() && userDetails.user.profileName == null) {
                                userDetails.user.profileName = displayName
                            }
                            userDetails.user.tokenId = tokenId
                            userDetails.user.loginType = type
                            userDetails.user.lastUpdated = System.currentTimeMillis()
                            repository.insertUserRecent(userDetails.user)
                            val profile = userDetails.profile
                            if (profile != null) {
                                storage.updateProfile(profile)
                            }
                            return@Listener
                        } else {
                            Log.d(TAG, "No recent values in server")
                        }
                    } catch (e: IllegalAccessException) {
                        Log.e(TAG, "JSONException ", e)
                    } catch (e: InstantiationException) {
                        Log.e(TAG, "JSONException ", e)
                    } catch (e: JSONException) {
                        Log.e(TAG, "JSONException ", e)
                    } catch (e: Exception) {
                        Log.e(TAG, "other error ", e)
                    }
                    // No user details available or some error
                    // create new user object by default
                    repository.insertUserRecent(repository.getNewUserDetails(tokenId, displayName, type))
                }, Response.ErrorListener { error ->
            Log.d(TAG, "Volley Error: $error")
            VolleySingleton.getInstance(context).logError(error)
            // Insert new user for all other conditions
            repository.insertUserRecent(repository.getNewUserDetails(tokenId, displayName, type))
        }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers: MutableMap<String, String> = HashMap()
                val authValue = "Bearer $tokenId"
                headers["Authorization"] = authValue
                //headers.put("Content-Type", "application/json; charset=utf-8");
                headers["accept"] = "text/plain"
                return headers
            }
        }
        VolleySingleton.getInstance(context).addJSONRequestToQueue(jsonObjectRequest)
    }

    // GET /api/Dashboard/GetFEDetailsByTeamId?teamId=2
    fun getFanEngageDataForTeam(tokenId: String, teamIdServer: Long) {
        Log.d(TAG, Constant.BASEURL + Constant.GET_FE_DETAILS + TEAM_QUERY + teamIdServer)
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(Method.GET,
                Constant.BASEURL + Constant.GET_FE_DETAILS + TEAM_QUERY + teamIdServer, null,
                Response.Listener { response ->
                    Log.d(TAG, response.toString())
                    try {
                        val fanEngageData: FanEngageData? = getInstance<FanEngageData>(FanEngageData::class.java, response.toString())
                        if (fanEngageData != null) {
                            repository.updateFanEngageData(
                                    fanEngageData.fanData,
                                    fanEngageData.waveData,
                                    fanEngageData.whistleData
                            )
                            return@Listener
                        } else {
                            Log.d(TAG, "FanEngageData not available for team selected")
                        }
                    } catch (e: IllegalAccessException) {
                        Log.e(TAG, "JSONException ", e)
                    } catch (e: InstantiationException) {
                        Log.e(TAG, "JSONException ", e)
                    } catch (e: JSONException) {
                        Log.e(TAG, "JSONException ", e)
                    } catch (e: Exception) {
                        Log.e(TAG, "other error ", e)
                    }

                    // Reset under all other conditions
                    repository.resetFanEngageData()
                }, Response.ErrorListener { error ->
            Log.d(TAG, "Volley Error: $error")
            VolleySingleton.getInstance(context).logError(error)
            // Reset under all other conditions
            repository.resetFanEngageData()
        }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers: MutableMap<String, String> = HashMap()
                val authValue = "Bearer $tokenId"
                headers["Authorization"] = authValue
                //headers.put("Content-Type", "application/json; charset=utf-8");
                headers["accept"] = "text/plain"
                return headers
            }
        }
        VolleySingleton.getInstance(context).addJSONRequestToQueue(jsonObjectRequest)
    }

    companion object {
        private const val TAG = "TeamRepository"
        private const val TEAM_QUERY = "?teamId="
    }
}