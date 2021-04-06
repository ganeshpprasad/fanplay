package com.fanplayiot.core.remote.pojo.fcm

import com.fanplayiot.core.remote.pojo.BaseData
import org.json.JSONArray
import org.json.JSONObject

class RegisterUser : BaseData<RegisterUser>() {
    var deviceToken : String? = null
    var tags: List<String>? =  null

    /*
    {
  "platform": "string",
  "devicetoken": "string",
  "tags": [
    "string"
  ]
}
     */
    public override fun getJSONObject(): JSONObject? {
        try {
            if (deviceToken == null) return null
            if (tags == null || tags!!.isEmpty()) return null
            val arr = JSONArray();

            tags?.forEach { tag ->
                arr.put(tag)
            }
            return JSONObject().apply {
                put("platform", "fcm")
                put("devicetoken", deviceToken)
                put("tags", arr)
            }

        } catch (e : Exception) {

        }
        return null
    }

    public override fun fromJSONObject(jsonObject: JSONObject?): RegisterUser? {
        return null
    }
}