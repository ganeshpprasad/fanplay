package com.fanplayiot.core.remote.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.fanplayiot.core.db.local.repository.PointsRepository
import com.fanplayiot.core.remote.RESPONSE
import com.fanplayiot.core.remote.STATUS_CODE
import com.fanplayiot.core.remote.VolleySingleton
import com.fanplayiot.core.utils.Constant
import org.json.JSONException
import java.util.*

class PointsMasterRepository(private val context: Context, private val pointsRepository: PointsRepository) {

    fun getRnRPointsStatus(referredSid: Long, receiverSid: Long, tokenId: String) {
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(Method.GET,
                getGETURLWithParams(referredSid, receiverSid), null,
                Response.Listener { response ->
                    Log.d(TAG, response.toString())
                    try {
                        val statusCode = response.optInt(STATUS_CODE, 0)
                        val responseObj = response.optJSONObject(RESPONSE)
                        if (statusCode == 200 && responseObj != null) {
                            val resultArr = responseObj.optJSONArray("result")

                            if (resultArr != null && resultArr.length() > 0
                                    && !resultArr.getJSONObject(0).getBoolean("rewardstatus")) {
                                postRnRPoints(referredSid, receiverSid, tokenId)
                            }
                        }
                    } catch (je: JSONException) {
                        Log.d(TAG, "json error", je)
                    } catch (e: Exception) {
                        Log.e(TAG, "error", e)
                    }
                }, Response.ErrorListener { error ->
            Log.e(TAG, "error " + error.message, error)
        }) {
            override fun getHeaders(): Map<String, String> {
                val headers: MutableMap<String, String> = HashMap()
                headers["accept"] = "text/plain"
                return headers
            }

        }
        VolleySingleton.getInstance(context).addJSONRequestToQueue(jsonObjectRequest)


    }

    private fun postRnRPoints(referredSid: Long, receiverSid: Long, tokenId: String) {
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(Method.POST,
                getPOSTURLWithParams(referredSid, receiverSid), null,
                Response.Listener { response ->
                    Log.d(TAG, response.toString())
                    try {
                        val statusCode = response.optInt(STATUS_CODE, 0)
                        if (statusCode == 200) {
                            pointsRepository.clearReferralSid()
                        }
                    } catch (je: JSONException) {
                        Log.d(TAG, "json error", je)
                    } catch (e: Exception) {
                        Log.e(TAG, "error", e)
                    }
                }, Response.ErrorListener { error ->
            Log.e(TAG, "error " + error.message, error)
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

    private fun getPOSTURLWithParams(referredSid: Long, receiverSid: Long): String {
        val builtUri = Uri.parse(Constant.BASEURL + Constant.POST_RNR_POINTS)
                .buildUpon().apply {
                    appendQueryParameter("referedsid", referredSid.toString())
                    appendQueryParameter("receiverid", receiverSid.toString())
                    appendQueryParameter("pointtype", "1") // For Reward for referral point type is 1
                }
        return builtUri.build().toString()
    }

    private fun getGETURLWithParams(referredSid: Long, receiverSid: Long): String {
        val builtUri = Uri.parse(Constant.BASEURL + Constant.GET_RNR_POINTS_STATUS)
                .buildUpon().apply {
                    appendQueryParameter("referedsid", referredSid.toString())
                    appendQueryParameter("receiverid", receiverSid.toString())
                    appendQueryParameter("pointtype", "1") // For Reward for referral point type is 1
                }
        return builtUri.build().toString()
    }

    companion object {
        private const val TAG = "PointsMasterRepository"
    }
}