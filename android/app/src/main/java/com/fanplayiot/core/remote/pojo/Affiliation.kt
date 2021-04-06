package com.fanplayiot.core.remote.pojo

import org.json.JSONObject

class Affiliation : BaseData<Affiliation>() {
    var id: Int? = null
    var storelink : String? = null

    override fun getJSONObject(): JSONObject? {
        return null
    }

    override fun fromJSONObject(jsonObject: JSONObject?): Affiliation {
        val affObj = jsonObject?.getJSONObject("response")?.optJSONArray("result")?.getJSONObject(0)
        id = affObj?.optInt("affiliationid", 0)
        storelink = affObj?.optString("affiliationstorelink", "")
        return this
    }
}