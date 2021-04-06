package com.fanplayiot.core.remote.pojo

import com.fanplayiot.core.db.local.entity.SponsorAnalytics
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class AdsAnalytics : BaseData<AdsAnalytics>() {
    /*
    {
  "teamId": 0,
  "sponsorAnalytics": [
    {
      "sponsorId": 0,
      "locationId": 0,
      "noOfClicks": 0,
      "screenTime": "2020-10-07T10:58:14.991Z"
    }
  ]
}
     */
    private var teamIdCheered = 1L
    private val array = JSONArray()
    fun setTeamIdCheered(teamIdCheered: Long) {
        this.teamIdCheered = teamIdCheered
    }

    @Throws(JSONException::class)
    fun addToAnalyticsArray(item: SponsorAnalytics) {
        val objJson = JSONObject()
        objJson.put("sponsorId", item.id)
        objJson.put("locationId", item.locationId)
        objJson.put("noOfClicks", item.noOfClicks)
        var screemTime: Long = 0
        try {
            screemTime = item.screenTime.toLong()
        } catch (ignored: NumberFormatException) {
        }
        objJson.put("screenTime", screemTime)
        array.put(objJson)
    }

    public override fun getJSONObject(): JSONObject? {
        try {
            if (array.length() == 0) return null
            val analyticJson = JSONObject()
            analyticJson.put("teamId", teamIdCheered)
            analyticJson.put("sponsorAnalytics", array)
            return analyticJson
        } catch (je: JSONException) {
            // log error
        }
        return null
    }

    @Throws(JSONException::class)
    override fun fromJSONObject(jsonObject: JSONObject?): AdsAnalytics? {
        return null
    }
}