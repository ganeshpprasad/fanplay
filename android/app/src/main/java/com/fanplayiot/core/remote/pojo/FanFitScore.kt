package com.fanplayiot.core.remote.pojo

import org.json.JSONObject

public class FanFitScore: BaseData<FanFitScore>() {
    var avgUserFFScore: Float = 0.0f
        private set
    var avgFFScore: Float = 0.0f
        private set
    //var highestUserFEScore: Float = 0.0f
    //    private set
    //var highestUserFFScore: Float = 0.0f
    //    private set
    var totalPoints: Long = 0L
        private set
    var totalFFPoints: Long = 0L
        private set

    override fun getJSONObject(): JSONObject? {
        return null
    }

    override fun fromJSONObject(jsonObject: JSONObject?): FanFitScore {
        val jsonArray = jsonObject?.optJSONObject("response")
                ?.getJSONArray("result")
        if (jsonArray?.length() ?: 0 > 0) {
            val fanObj = jsonArray?.getJSONObject(0)
            avgUserFFScore = fanObj?.optDouble("avguserffscore", 0.0)?.toFloat() ?: 0.0f
            avgFFScore = fanObj?.optDouble("avgffscore", 0.0)?.toFloat() ?: 0.0f
            totalFFPoints = fanObj?.optLong("fanfitpoints", 0L) ?: 0L
            totalPoints = fanObj?.optLong("totalpoints", 0L) ?: 0L
        }
        return this
    }
}

/*
{
  "response": {
    "result": [
      {
        "higestuserfescore": 6.8,
        "highestuserffScore": 7,
        "avgffscore": 8.64,
        "avguserffscore": 6
      }
    ]
  },
  "statuscode": 200,
  "message": "",
  "callstarttime": "2020-11-30 11:39",
  "callendtime": "2020-11-30 11:39"
}

 */
