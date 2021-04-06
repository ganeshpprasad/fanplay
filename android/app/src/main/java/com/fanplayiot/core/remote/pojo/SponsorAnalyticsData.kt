package com.fanplayiot.core.remote.pojo

import com.fanplayiot.core.db.local.entity.SponsorAnalytics
import org.json.JSONException
import org.json.JSONObject

class SponsorAnalyticsData : BaseData<SponsorAnalyticsData?>() {
    /*
      "sponsorId": 0,
      "locationId": 0,
      "noOfClicks": 0,
      "screenTime": "2020-09-25T12:32:59.332Z"

     */
    private var sponsorAnalytics: SponsorAnalytics? = null

    fun setSponsorAnalytics(sponsorAnalytics: SponsorAnalytics?) {
        this.sponsorAnalytics = sponsorAnalytics
    }

    public override fun getJSONObject(): JSONObject? {
        try {
            if (sponsorAnalytics != null) {
                val analyticJson = JSONObject()
                analyticJson.put("sponsorId", sponsorAnalytics!!.id)
                analyticJson.put("locationId", sponsorAnalytics!!.locationId)
                analyticJson.put("noOfClicks", sponsorAnalytics!!.noOfClicks)
                analyticJson.put("screenTime", sponsorAnalytics!!.screenTime)
                return analyticJson
            }
        } catch (je: JSONException) {
            // log error
        }
        return null
    }

    @Throws(JSONException::class)
    public override fun fromJSONObject(jsonObject: JSONObject?): SponsorAnalyticsData? {
        return null
    }
}