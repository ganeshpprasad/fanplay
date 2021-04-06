package com.fanplayiot.core.remote.pojo

import android.util.Log
import com.fanplayiot.core.db.local.entity.FitnessBP
import com.fanplayiot.core.db.local.entity.FitnessHR
import com.fanplayiot.core.db.local.entity.FitnessSCD
import com.fanplayiot.core.db.local.entity.HeartRate.Companion.CAMERA
import com.fanplayiot.core.db.local.entity.HeartRate.Companion.DEVICE_BAND
import com.fanplayiot.core.db.local.entity.json.FitnessChallenge
import com.fanplayiot.core.db.local.entity.json.LocationDetails
import com.fanplayiot.core.remote.getResultArray
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class ChallengeDetails : BaseData<ChallengeDetails>() {

    companion object {
        private var DATE_PATTERN: String = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        private const val TAG = "ChallengeDetails"
    }

    enum class GETTYPE { LIST_SESSION_ID, GET_SESSION, GET_FITNESS_DATA }

    var fitnessChallenge: FitnessChallenge? = null
    var mode: Int = 0
    var startTs: Long? = null
    var stopTs: Long? = null
    var scdList: List<FitnessSCD?>? = null
    var hrList: List<FitnessHR?>? = null
    var bpList: List<FitnessBP?>? = null
    var outputType = GETTYPE.GET_SESSION

    var listOfSessionIds: List<Long>? = null
        private set

    private fun checkNull(): Boolean {
        return (fitnessChallenge != null && fitnessChallenge!!.sid > 0 &&
                fitnessChallenge!!.teamIdServer > 0 && fitnessChallenge!!.challengeId > 0 &&
                fitnessChallenge!!.videoId > 0 && mode > 0 && startTs != null && stopTs != null)
    }

    public override fun getJSONObject(): JSONObject? {
        if (!checkNull()) return null
        val format = SimpleDateFormat(DATE_PATTERN, Locale.getDefault())

        val jsonObject = JSONObject()
        jsonObject.put("sid", fitnessChallenge!!.sid)
        jsonObject.put("teamid", fitnessChallenge!!.teamIdServer)
        jsonObject.put("affiliationid", fitnessChallenge!!.affiliationId ?: 0)
        jsonObject.put("groupid", fitnessChallenge!!.groupId ?: 0)
        jsonObject.put("challengeid", fitnessChallenge!!.challengeId)
        jsonObject.put("challengevideoid", fitnessChallenge!!.videoId)

        val startObj = JSONObject()
        startObj.put("startsessiontimestamp", format.format(Date(startTs!!)))
        startObj.put("startsessionlatitude", fitnessChallenge!!.startSession?.latitude ?: 0.0)
        startObj.put("startsessionlongitude", fitnessChallenge!!.startSession?.longitude ?: 0.0)
        jsonObject.put("startsession", startObj)

        val stopObj = JSONObject()
        stopObj.put("endsessiontimestamp", format.format(Date(stopTs!!)))
        stopObj.put("endsessionlatitude", fitnessChallenge!!.stopSession?.latitude ?: 0.0)
        stopObj.put("endsessionlongitude", fitnessChallenge!!.stopSession?.longitude ?: 0.0)
        jsonObject.put("endsession", stopObj)

        val fitnessObject = JSONObject()

        val deviceType: Int = when (mode) {
            DEVICE_BAND -> 3
            CAMERA -> 1
            else -> mode
        }
        fitnessObject.put("devicetype", deviceType)
        if (hrList != null && hrList!!.isNotEmpty()) {
            val hrArray = JSONArray()
            hrList?.forEach {
                if (it == null) return@forEach
                val hrObj = JSONObject()
                hrObj.put("hr", it.heartRate)
                hrObj.put("datacollectedts", format.format(Date(it.lastUpdated)))
                hrArray.put(hrObj)
            }
            fitnessObject.put("hr", hrArray)
        }
        if (scdList != null && scdList!!.isNotEmpty()) {
            val scdArray = JSONArray()
            scdList?.forEach {
                if (it == null) return@forEach
                val scdObj = JSONObject()
                scdObj.put("steps", it.steps)
                scdObj.put("calorie", it.calories.toInt())
                val distance = (it.distance * 100.0).roundToInt().toDouble() / 100.0
                scdObj.put("distance", distance)
                scdObj.put("datacollectedts", format.format(Date(it.lastUpdated)))
                scdArray.put(scdObj)
            }
            fitnessObject.put("scd", scdArray)
        }
        if (bpList != null && bpList!!.isNotEmpty()) {
            val bpArray = JSONArray()
            bpList?.forEach {
                if (it == null) return@forEach
                val bpObj = JSONObject()
                bpObj.put("systolic", it.systolic)
                bpObj.put("diastolic", it.diastolic)
                bpObj.put("datacollectedts", format.format(Date(it.lastUpdated)))
                bpArray.put(bpObj)
            }
            fitnessObject.put("bp", bpArray)
        }
        jsonObject.put("fitnessdetails", fitnessObject)
        return jsonObject
    }

    public override fun fromJSONObject(jsonObject: JSONObject?): ChallengeDetails? {
        val chaArrObj = getResultArray(jsonObject, "challengeSessionDetails") ?: return null
        val format = SimpleDateFormat(DATE_PATTERN, Locale.getDefault())
        when (outputType) {
            GETTYPE.LIST_SESSION_ID -> {
                val sessionIds = mutableListOf<Long>()
                for (i in 0 until chaArrObj.length()) {
                    val item = chaArrObj.optJSONObject(i)
                    if (item.has("sessionid") && item.optLong("sessionid") > 0L) {
                        sessionIds.add(item.optLong("sessionid"))
                    }
                }
                listOfSessionIds = sessionIds
                return this
            }
            GETTYPE.GET_SESSION -> {
                for (i in 0 until chaArrObj.length()) {
                    val item = chaArrObj.optJSONObject(i)
                    val start: Long
                    val stop: Long
                    val startTsStr: String? = item.optString("startsessiontimestamp")
                    val stopTsStr: String? = item.optString("endsessiontimestamp")
                    if (startTsStr.isNullOrEmpty()) {
                        start = 0L
                    } else {
                        start = getTimeStamp(startTsStr, format) ?: 0L
                    }
                    if (stopTsStr.isNullOrEmpty()) {
                        stop = 0L
                    } else {
                        stop = getTimeStamp(stopTsStr, format) ?: 0L
                    }
                    val startTsTemp = startTs ?: 0L
                    val stopTsTemp = stopTs ?: 0L
                    if (item.has("sessionid") && item.optLong("sessionid") > 0L &&
                            start > 0L && (startTsTemp / 1000) == (start / 1000) &&
                            stop > 0L && (stopTsTemp / 1000) == (stop / 1000)) {
                        //Log.d(TAG, "start $start stop $stop")
                        fitnessChallenge = getFitnessChallenge(item)
                        Log.d(TAG, "fitnessChallenge sessionid ${fitnessChallenge?.sessionId}")
                        return this
                    }
                }
            }
            GETTYPE.GET_FITNESS_DATA -> {

            }
        }
        return null
    }

    private fun getFitnessChallenge(chaObj: JSONObject): FitnessChallenge {
        return FitnessChallenge(
                chaObj.optLong("sessionid", -1L),
                chaObj.optLong("sid", -1L),
                chaObj.optLong("teamid"),
                chaObj.optLong("affiliationid"),
                chaObj.optLong("groupid"),
                chaObj.optLong("challengeid", -1L),
                chaObj.optLong("challengevideoid", -1L),

                LocationDetails(
                        chaObj.optDouble("startsessionlatitude", 0.0),
                        chaObj.optDouble("startsessionlongitude", 0.0)),
                LocationDetails(
                        chaObj.optDouble("endsessionlatitude", 0.0),
                        chaObj.optDouble("endsessionlongitude", 0.0))
        )
    }
}

/*
{
  "sid": 0,
  "teamid": 0,
  "affiliationid": 0,
  "groupid": 0,
  "challengeid": 0,
  "challengevideoid": 0,
  "startsession": {
    "startsessiontimestamp": "2021-03-04T09:22:24.847Z",
    "startsessionlatitude": 0,
    "startsessionlongitude": 0
  },
  "endsession": {
    "endsessiontimestamp": "2021-03-04T09:22:24.847Z",
    "endsessionlatitude": 0,
    "endsessionlongitude": 0
  },
  "fitnessdetails": {
    "devicetype": 0,
    "hr": [
      {
        "hr": 0,
        "datacollectedts": "2021-03-04T09:22:24.847Z"
      }
    ],
    "scd": [
      {
        "steps": 0,
        "calorie": 0,
        "distance": 0,
        "datacollectedts": "2021-03-04T09:22:24.847Z"
      }
    ],
    "bp": [
      {
        "systolic": 0,
        "diastolic": 0,
        "datacollectedts": "2021-03-04T09:22:24.847Z"
      }
    ]
  }
}
 */