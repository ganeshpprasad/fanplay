package com.fanplayiot.core.remote.pojo

import com.fanplayiot.core.db.local.entity.SponsorData
import com.fanplayiot.core.remote.RESPONSE
import com.fanplayiot.core.remote.STATUS_CODE
import org.json.JSONException
import org.json.JSONObject

class Sponsors : BaseData<Sponsors>() {
    var advertisers: Array<SponsorData>? = null
        private set
    var analytics: SponsorAnalyticsData? = null
    var isEmptySponsors = false
        private set

    public override fun getJSONObject(): JSONObject? {
        return null
    }

    @Throws(JSONException::class)
    public override fun fromJSONObject(jsonObject: JSONObject?): Sponsors? {
        val adArray = mutableListOf<SponsorData>()
        // Check for null
        if (jsonObject == null) return null
        if (jsonObject.optJSONObject(RESPONSE) == null) return null
        if (jsonObject.optInt(STATUS_CODE, 0) == 200 &&
                (jsonObject.getJSONObject(RESPONSE)
                        .optJSONArray("sponsorData") == null ||
                        jsonObject.getJSONObject("response")
                                .getJSONArray("sponsorData").length() == 0)) {
            analytics = null
            advertisers = null
            isEmptySponsors = true
            return this
        }
        val sponsorDataArray = jsonObject.getJSONObject("response")
                .getJSONArray("sponsorData")
        val array = sponsorDataArray.getJSONObject(0).optJSONArray("sponsors")
        if (array == null || array.length() <= 0) {
            analytics = null
            advertisers = null
            isEmptySponsors = true
            return this
        }
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            val adId = item.optInt("sponsorid", -1)
            val locId = item.optInt("locationid", -1)
            var adUrl = item.getString("sponsoradurl")
            if (adUrl == "null") adUrl = ""
            var cta = item.getString("clicktoactionurl")
            if (cta == "null") cta = ""
            if (adId != -1 && locId != -1) {
                val advertiser = SponsorData(adId, adUrl, cta, locId)
                adArray.add(advertiser)
            }
        }
        advertisers = adArray.toTypedArray()
        return this
    } /*
    {
            "sponsorid": 1,
            "locationid": 1,
            "campaignid": 1,
            "sponsoradurl": "https://fangurudevstrg.blob.core.windows.net/csk-ipl/sponorad-1_Ad_Banner_1388_380-01.png",
            "clicktoactionurl": "https://www.yahoo.com/"
          }
     */
}