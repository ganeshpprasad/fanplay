package com.fanplayiot.core.remote

import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

const val RESPONSE = "response"
const val STATUS_CODE = "statuscode"
const val RESULT = "result"

fun getFirstResult(response: JSONObject?, resultName: String? = RESULT) : JSONObject? =
    try {
        val statusCode = response?.optInt(STATUS_CODE, 0)
        val responseObj = response?.optJSONObject(RESPONSE)
        if (statusCode == 200 && responseObj != null) {
            responseObj.optJSONArray(resultName)?.optJSONObject(0)
        } else {
            null
        }

    } catch (je: JSONException) {
        Log.d("getFirstResult", "json error", je)
        null
    } catch (e: Exception) {
        Log.e("getFirstResult", "error", e)
        null
    }


fun getResultArray(response: JSONObject?, resultName: String? = RESULT) : JSONArray? =
        try {
            val statusCode = response?.optInt(STATUS_CODE, 0)
            val responseObj = response?.optJSONObject(RESPONSE)
            if (statusCode == 200 && responseObj != null) {
                responseObj.optJSONArray(resultName)
            } else {
                null
            }

        } catch (je: JSONException) {
            Log.d("getFirstResult", "json error", je)
            null
        } catch (e: Exception) {
            Log.e("getFirstResult", "error", e)
            null
        }

