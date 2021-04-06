package com.fanplayiot.core.remote.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.volley.*
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.fanplayiot.core.background.OfflineSyncWork
import com.fanplayiot.core.background.io.JsonFileIO
import com.fanplayiot.core.background.io.OfflineJson
import com.fanplayiot.core.db.local.dao.HomeDao
import com.fanplayiot.core.db.local.entity.*
import com.fanplayiot.core.db.local.repository.FanEngageRepository
import com.fanplayiot.core.remote.VolleySingleton
import com.fanplayiot.core.remote.getFirstResult
import com.fanplayiot.core.remote.pojo.BaseData.Companion.getInstance
import com.fanplayiot.core.remote.pojo.FanEmote
import com.fanplayiot.core.remote.pojo.FanEngagement
import com.fanplayiot.core.utils.Constant
import com.fanplayiot.core.utils.Helpers
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.*

class MainRepository(private val context: Context, private val homeDao: HomeDao, private val fanEngageRepository: FanEngageRepository) {
    var band: LiveData<Device?>
    var fanEmoteLive = MutableLiveData<FanEmote?>(null)
    fun postFanEngagement(heartRate: HeartRate, hrZone: Int, affiliationId: Long,
                          fanData: FanData,
                          waveData: WaveData?,
                          whistleData: WhistleData?) {
        val user = homeDao.userData
        if (user == null) {
            Log.e(TAG, "User is null")
            return
        }
        if (user.sid == null) {
            Log.e(TAG, "User sid is null")
            return
        }
        val idToken = homeDao.userData!!.tokenId
        val fe = FanEngagement()
        fe.setUser(user)
        fe.setHeartRate(heartRate)
        val teams = homeDao.getAllTeam()
        if (teams != null && teams.size > 0) {
            val teamIdServer = teams[0].teamIdServer
            fe.setTeamIdCheered(teamIdServer!!)
        } else {
            fe.setTeamIdCheered(1)
        }
        val players = homeDao.getAllPlayer()
        if (players == null) {
            Log.e(TAG, "players is null")
            return
        }
        fe.setPlayers(players)
        fe.setFanData(fanData)
        fe.setHrZone(hrZone)
        fe.setAffiliationId(affiliationId)
        if (waveData != null) fe.setWaveData(waveData)
        if (whistleData != null) fe.setWhistleData(whistleData)
        if (band.value != null) fe.setDevice(band.value!!)
        val feJson = fe.getJSONObject()
        val sid = user.sid!!
        if (!checkOnline(feJson!!, sid)) return
        //Log.d(TAG, idToken);
        Log.d(TAG, feJson.toString())
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(Method.POST,
                Constant.BASEURL + Constant.POST_FANENGAGEMENT, feJson,
                Response.Listener { response ->
                    Log.d(TAG, response.toString())
                    try {
                        val statusCode = response.getInt("statuscode")
                        if (statusCode == 200) {
                            fanEngageRepository.updatePlayerToClearCounts()
                        }
                    } catch (je: JSONException) {
                        Log.d(TAG, "json error")
                    } catch (e: Exception) {
                        Log.e(TAG, "error", e)
                    }
                }, Response.ErrorListener { error ->
            Log.d(TAG, "Volley Error: $error")
            VolleySingleton.getInstance(context).handleError(error)
            if (error is NetworkError || error is ServerError || error is TimeoutError) {
                fanEngageOffline(feJson, sid)
            }
        }) {
            override fun getHeaders(): Map<String, String> {
                val headers: MutableMap<String, String> = HashMap()
                val authValue = "Bearer $idToken"
                headers["Authorization"] = authValue
                headers["Content-Type"] = "application/json; charset=utf-8"
                return headers
            }
        }
        VolleySingleton.getInstance(context).addJSONRequestToQueue(jsonObjectRequest)
    }

    private fun checkOnline(feJson: JSONObject, sid: Long): Boolean {
        if (!Helpers.isInternetAvailable(context)) {
            fanEngageOffline(feJson, sid)
            return false
        }
        return true
    }

    private fun fanEngageOffline(feJson: JSONObject, sid: Long) {
        // Write Fan engagement request JSON with sid to file
        val feFile = JsonFileIO()
        val internalFileDir = context.filesDir
        val txtFile = File(internalFileDir.toString() + File.separator + OfflineSyncWork.FE_FILE_NAME)
        try {
            // create new file in internal files dir if not exist
            if (!txtFile.exists()) {
                txtFile.createNewFile()
            }
            var list = feFile.readJsonStream(txtFile)
            if (list == null) {
                list = ArrayList()
            }

            // Read file and add to the list
            feJson.put("sid", sid)
            val feOffline = OfflineJson()
            feOffline.jsonString = feJson.toString()
            list.add(feOffline)
            Log.d(TAG, feJson.toString())

            // Write list back to file
            feFile.writeJsonStream(txtFile, list)
            fanEngageRepository.updatePlayerToClearCounts()
            OfflineSyncWork.startOfflineSyncWork(context)
        } catch (e: Exception) {
            Log.e(TAG, "error", e)
        }
    }

    fun postFanEngagementOffline(feJson: JSONArray) {
        val user = homeDao.userData
        if (user == null) {
            Log.e(TAG, "User is null")
            return
        }
        Log.d(TAG, feJson.toString())
        val idToken = homeDao.userData!!.tokenId
        val jsonArrayRequest: JsonArrayRequest = object : JsonArrayRequest(Method.POST,
                Constant.BASEURL + Constant.POST_SYNC_FANENGAGEMENT, feJson,
                Response.Listener { response -> Log.d(TAG, "response: $response") },
                Response.ErrorListener { error ->
                    if (error is ParseError) {
                        val response = error.toString()
                        if (response.matches(Regex.fromLiteral(".*[\"response\":null,\"statuscode\":200].*"))) {
                            val internalFileDir = context.filesDir
                            val feFile = File(internalFileDir.toString() + File.separator + OfflineSyncWork.FE_FILE_NAME)
                            feFile.delete()
                            Log.d(TAG, "Sync success")
                        }
                        return@ErrorListener
                    }
                    Log.d(TAG, "Volley Error: $error")
                }) {
            override fun getHeaders(): Map<String, String> {
                val headers: MutableMap<String, String> = HashMap()
                val authValue = "Bearer $idToken"
                headers["Authorization"] = authValue
                headers["Content-Type"] = "application/json; charset=utf-8"
                return headers
            }
        }
        VolleySingleton.getInstance(context).addJSONArrayRequestToQueue(jsonArrayRequest)
    }

    fun getFanEmote() {
            val team = homeDao.getDefaultTeam()
            if (team == null || team.teamIdServer == null) return
            val teamId = team.teamIdServer!!
            val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(Method.GET,
                    Constant.BASEURL + Constant.GET_FANEMOTE + TEAM_QUERY + teamId + DURATION_QUERY, null,
                    Response.Listener { response -> //Log.d(TAG, response.toString());
                        try {
                            val fanEmote: FanEmote? = getInstance<FanEmote>(FanEmote::class.java, response.toString())
                            if (fanEmote == null) {
                                Log.e(TAG, "fan emote is null")
                                return@Listener
                            }
                            fanEmoteLive.postValue(fanEmote)
                        } catch (iae: IllegalAccessException) {
                            Log.e(TAG, "error ", iae)
                        } catch (ie: InstantiationException) {
                            Log.e(TAG, "InstantiationException ", ie)
                        } catch (e: JSONException) {
                            Log.e(TAG, "JSONException ", e)
                        } catch (ex: Exception) {
                            Log.e(TAG, "Exception ", ex)
                        }
                    }, Response.ErrorListener { error ->
                Log.d(TAG, "Volley Error: $error")
                VolleySingleton.getInstance(context).handleError(error)
            }) {
                override fun getHeaders(): Map<String, String> {
                    val headers: MutableMap<String, String> = HashMap()
                    headers["accept"] = "text/plain"
                    return headers
                }
            }
            VolleySingleton.getInstance(context).addJSONRequestToQueue(jsonObjectRequest)
        }

    fun getFanEmoteResponse(onSuccess: (String?) -> Unit) {
            val team = homeDao.getDefaultTeam()
            if (team == null || team.teamIdServer == null) return
            val teamId = team.teamIdServer!!
            val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(Method.GET,
                    Constant.BASEURL + Constant.GET_FANEMOTE + TEAM_QUERY + teamId + DURATION_QUERY, null,
                    Response.Listener { response ->
                        Log.d(TAG, response.toString());
                        try {
                            onSuccess.invoke(getFirstResult(response, "fanemote")?.toString())
                        } catch (iae: IllegalAccessException) {
                            Log.e(TAG, "error ", iae)
                        } catch (ie: InstantiationException) {
                            Log.e(TAG, "InstantiationException ", ie)
                        } catch (e: JSONException) {
                            Log.e(TAG, "JSONException ", e)
                        } catch (ex: Exception) {
                            Log.e(TAG, "Exception ", ex)
                        }
                    }, Response.ErrorListener { error ->
                Log.d(TAG, "Volley Error: $error")
                VolleySingleton.getInstance(context).handleError(error)
            }) {
                override fun getHeaders(): Map<String, String> {
                    val headers: MutableMap<String, String> = HashMap()
                    headers["accept"] = "text/plain"
                    return headers
                }
            }
            VolleySingleton.getInstance(context).addJSONRequestToQueue(jsonObjectRequest)
        }

    fun getFEDetailsByTeamId(onSuccess: (String?) -> Unit) {
        val team = homeDao.getDefaultTeam()
        if (team == null || team.teamIdServer == null) return
        val teamId = team.teamIdServer!!
        val tokenId = homeDao.userData?.tokenId ?: return
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(Method.GET,
                Constant.BASEURL + Constant.GET_FE_DETAILS + TEAM_QUERY + teamId, null,
                Response.Listener { response ->
                    Log.d(TAG, response.toString());
                    try {
                        onSuccess.invoke(getFirstResult(response)?.toString())
                    } catch (iae: IllegalAccessException) {
                        Log.e(TAG, "error ", iae)
                    } catch (ie: InstantiationException) {
                        Log.e(TAG, "InstantiationException ", ie)
                    } catch (e: JSONException) {
                        Log.e(TAG, "JSONException ", e)
                    } catch (ex: Exception) {
                        Log.e(TAG, "Exception ", ex)
                    }
                }, Response.ErrorListener { error ->
            Log.d(TAG, "Volley Error: $error")
            VolleySingleton.getInstance(context).handleError(error)
        }) {
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
        const val TAG = "MainRepository"
        private const val TEAM_QUERY = "?teamId="
        private const val DURATION_QUERY = "&duration=86400"
    }

    init {
        band = fanEngageRepository.band
    }
}