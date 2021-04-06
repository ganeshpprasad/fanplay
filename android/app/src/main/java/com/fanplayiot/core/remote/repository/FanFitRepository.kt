package com.fanplayiot.core.remote.repository

import android.content.Context
import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.fanplayiot.core.db.local.entity.*
import com.fanplayiot.core.db.local.entity.json.FitnessChallenge
import com.fanplayiot.core.db.local.repository.FitnessRepository
import com.fanplayiot.core.remote.VolleySingleton
import com.fanplayiot.core.remote.pojo.BaseData.Companion.getInstance
import com.fanplayiot.core.remote.pojo.ChallengeDetails
import com.fanplayiot.core.remote.pojo.FanFitScore
import com.fanplayiot.core.remote.pojo.FanFitness
import com.fanplayiot.core.utils.Constant
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.json.JSONException
import java.util.*

class FanFitRepository(private val context: Context, private val repository: FitnessRepository) {

    suspend fun postFitness(user: User, scdList: List<FitnessSCD?>,
                            hrList: List<FitnessHR?>,
                            bpList: List<FitnessBP?>, type: Int, scope: CoroutineScope) {
        if (user.sid == null) {
            Log.d(TAG, "user sid is null")
            return
        }
        val tokenId = user.tokenId
        val fanFitness = FanFitness()
        fanFitness.setSid(user.sid!!)
        fanFitness.setScdList(scdList)
        fanFitness.setHrList(hrList)
        fanFitness.setBpList(bpList)
        fanFitness.setDeviceType(type)
        val fitJson = fanFitness.getJSONObject() ?: return
        Log.d(TAG, fitJson.toString())
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(Method.POST,
                Constant.BASEURL + Constant.POST_FAN_FITNESS, fitJson,
                Response.Listener { response ->
                    Log.d(TAG, response.toString())
                    try {
                        val code = response.getInt("statuscode")
                        Log.d(TAG, "Status code postFitness $code")
                        if (code == 200) {
                            repository.onSuccessPostFitness(scdList, hrList, bpList, scope)
                        }
                        //result.postValue(code == 200);
                    } catch (je: JSONException) {
                        Log.d(TAG, "json error")
                    } catch (e: Exception) {
                        Log.e(TAG, "error", e)
                    }
                }, Response.ErrorListener { error ->
            Log.e(TAG, "error " + error.message, error)
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

    fun getAllFitness(user: User, type: Int, pageId: Int) {
        if (user.sid == null) {
            Log.d(TAG, "user sid is null")
            return
        }
        val tokenId = user.tokenId
        var url = Constant.BASEURL
        if (type == FitnessRepository.QUERY_SCD) {
            url = url + Constant.GET_SCD_FITNESS + SID_QUERY + user.sid + PAGEID_QUERY + pageId
        } else if (type == FitnessRepository.QUERY_HR) {
            url = url + Constant.GET_HR_FITNESS + SID_QUERY + user.sid + PAGEID_QUERY + pageId
        } else if (type == FitnessRepository.QUERY_BP) {
            url = url + Constant.GET_BP_FITNESS + SID_QUERY + user.sid + PAGEID_QUERY + pageId
        }
        Log.d(TAG, url)
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(Method.GET,
                url, null,
                Response.Listener { response ->
                    Log.d(TAG, response.toString())
                    try {
                        val fanFitness: FanFitness? = getInstance(FanFitness::class.java, response.toString())
                        if (fanFitness == null) {
                            Log.d(TAG, "fanFitness is null")
                            return@Listener
                        }
                        if (type == FitnessRepository.QUERY_SCD && fanFitness.scdList != null && !fanFitness.scdList!!.isEmpty()) {
                            repository.onSuccessGetSCD(fanFitness.scdList!!)
                        } else if (type == FitnessRepository.QUERY_HR && fanFitness.hrList != null && !fanFitness.hrList!!.isEmpty()) {
                            repository.onSuccessGetHR(fanFitness.hrList!!)
                        } else if (type == FitnessRepository.QUERY_BP && fanFitness.bpList != null && !fanFitness.bpList!!.isEmpty()) {
                            repository.onSuccessGetBP(fanFitness.bpList!!)
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
                }, Response.ErrorListener { error -> Log.d(TAG, "Volley Error: $error") }) {
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

    fun getFanFitScores(user: User) {
        if (user.sid == null) {
            Log.d(TAG, "user sid is null")
            return
        }
        val tokenId = user.tokenId
        var url = Constant.BASEURL
        url = url + Constant.GET_FANFIT_SCORES + SID_QUERY + user.sid
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(Method.GET,
                url, null,
                Response.Listener { response ->
                    Log.d(TAG, response.toString())
                    try {
                        val fanFitScore: FanFitScore? = getInstance<FanFitScore>(FanFitScore::class.java, response.toString())
                        if (fanFitScore == null) {
                            Log.d(TAG, "fanFitScore is null")
                            return@Listener
                        }
                        repository.updateFanFitScore(fanFitScore)
                    } catch (e: IllegalAccessException) {
                        Log.e(TAG, "JSONException ", e)
                    } catch (e: InstantiationException) {
                        Log.e(TAG, "JSONException ", e)
                    } catch (e: JSONException) {
                        Log.e(TAG, "JSONException ", e)
                    } catch (e: Exception) {
                        Log.e(TAG, "other error ", e)
                    }
                }, Response.ErrorListener { error -> Log.d(TAG, "Volley Error: $error") }) {
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

    fun postChallengeEnd(id: Long, tokenId: String, context: Context, challenge: FitnessActivity,
                         mode: Int, scdList:
                         List<FitnessSCD?>, hrList: List<FitnessHR?>, bpList: List<FitnessBP?>,
                         scope: CoroutineScope) {
        val details = ChallengeDetails()
        details.fitnessChallenge = Json.decodeFromString<FitnessChallenge>(challenge.commonJson
                ?: "")
        details.mode = mode
        details.startTs = challenge.start
        details.stopTs = challenge.end
        details.scdList = scdList
        details.hrList = hrList
        details.bpList = bpList

        val jsonObj = details.getJSONObject() ?: run {
            Log.d(TAG, "json is null")
            return
        }
        //Log.d(TAG, "Challenge Post : ${jsonObj.toString()}")
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(Method.POST,
                Constant.BASEURL + Constant.POST_CHALLENGE, jsonObj,
                Response.Listener { response ->
                    Log.d(TAG, response.toString())
                    try {
                        val code = response.getInt("statuscode")
                        Log.d(TAG, "Status code postChallengeEnd $code")
                        if (code == 200) {
                            Log.d(TAG, "Status code $code")
                            repository.onSuccessPostFitness(scdList, hrList, bpList, scope)
                            details.fitnessChallenge?.takeIf { cha ->
                                cha.challengeId > -1L
                            }?.sid?.let { sid ->
                                getChallengeSession(id = id, details)
                            }
                        }
                        //result.postValue(code == 200);
                    } catch (je: JSONException) {
                        Log.d(TAG, "json error")
                    } catch (e: Exception) {
                        Log.e(TAG, "error", e)
                    }
                }, Response.ErrorListener { error ->
            Log.e(TAG, "error " + error.message, error)
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

    fun getChallengeSession(id: Long, challengeDetails: ChallengeDetails) {
        if (challengeDetails.fitnessChallenge == null) {
            Log.d(TAG, "fitnessChallenge is null")
            return
        }
        if (challengeDetails.fitnessChallenge?.sid == null) {
            Log.d(TAG, "user sid is null")
            return
        }

        var url = Constant.BASEURL
        url = url + Constant.GET_CHALLENGE + SID_QUERY + challengeDetails.fitnessChallenge!!.sid +
                (if (challengeDetails.fitnessChallenge!!.challengeId > 0) {
                    CHALLENGE_QUERY + challengeDetails.fitnessChallenge!!.challengeId
                } else {
                    ""
                }) +
                (if (challengeDetails.fitnessChallenge!!.affiliationId ?: -1L > 0) {
                    AFFID_QUERY + challengeDetails.fitnessChallenge!!.affiliationId
                } else {
                    ""
                })
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(Method.GET,
                url, null,
                Response.Listener { response ->
                    Log.d(TAG, response.toString())
                    try {

                        challengeDetails.outputType = ChallengeDetails.GETTYPE.GET_SESSION
                        val details: ChallengeDetails? = challengeDetails.fromJSONObject(response)
                        details?.fitnessChallenge?.sessionId?.let {
                            repository.updateSessionId(id, details.fitnessChallenge!!)
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
                }, Response.ErrorListener { error -> Log.d(TAG, "Volley Error: $error") }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers: MutableMap<String, String> = HashMap()
                headers["accept"] = "text/plain"
                return headers
            }
        }
        VolleySingleton.getInstance(context).addJSONRequestToQueue(jsonObjectRequest)
    }

    companion object {
        private const val TAG = "FanFitRepository"
        private const val SID_QUERY = "?sid="
        private const val CHALLENGE_QUERY = "&challengeid="
        private const val AFFID_QUERY = "&affiliationid="

        //private static final String TYPE_QUERY = "&devicetype=";
        private const val PAGEID_QUERY = "&pageid="
    }
}