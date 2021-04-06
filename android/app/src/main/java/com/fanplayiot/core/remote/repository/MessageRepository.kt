package com.fanplayiot.core.remote.repository

import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.fanplayiot.core.remote.VolleySingleton
import com.fanplayiot.core.remote.pojo.fcm.RegisterUser
import com.fanplayiot.core.utils.Constant
import org.json.JSONException
import java.util.*

class MessageRepository(private val volley: VolleySingleton) {

    fun registerUser(tokenId: String, deviceToken: String, tags: List<String>) {
        val registerUser = RegisterUser().apply {
            this.deviceToken = deviceToken
            this.tags = tags
        }
        val jsonObject = registerUser.getJSONObject()
        //Log.d(TAG, jsonObject.toString())
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(Method.POST,
                Constant.BASEURL + Constant.POST_REGISTER_FCM, jsonObject,
                Response.Listener { response ->
                    //Log.d(TAG, response.toString())
                    try {
                        val statusCode = response.getInt("statuscode")
                        if (statusCode == 200) {

                        }
                    } catch (je: JSONException) {
                        Log.d(TAG, "json error")
                    } catch (e: Exception) {
                        Log.e(TAG, "error", e)
                    }
                }, Response.ErrorListener { error ->
            Log.e(TAG, "error " + error.message, error)
            volley.handleError(error)
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
        jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
                VOLLEY_TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)

        volley.addJSONRequestToQueue(jsonObjectRequest)
    }

    companion object {
        private const val TAG = "MessageRepository"
        private const val VOLLEY_TIMEOUT_MS = 30000
    }
}