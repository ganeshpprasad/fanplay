package com.fanplayiot.core.db.local.entity

import org.json.JSONException
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat

abstract class BaseData<T> {
    protected abstract fun getJSONObject(): JSONObject?
    @Throws(JSONException::class)
    protected abstract fun fromJSONObject(jsonObject: JSONObject?): T?

    @Suppress("UNCHECKED_CAST")
    companion object {
        @JvmStatic
        @Throws(IllegalAccessException::class, InstantiationException::class, JSONException::class)
        fun <T> getInstance(theClass: Class<T>, json: String?): T? {
            val `object` = theClass.newInstance()
            val baseData = `object` as BaseData<*>
            return baseData.fromJSONObject(JSONObject(json ?: "")) as T?
        }
    }
}

private const val DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
private const val DATE_PATTERN1 = "yyyy-MM-dd'T'HH:mm:ss.SSS"
private const val DATE_PATTERN2 = "yyyy-MM-dd'T'HH:mm:ss"

@Throws(ParseException::class)
fun getTimeStamp(dateStr: String, format: SimpleDateFormat): Long? {
    try {
        format.applyPattern(DATE_PATTERN)
        val ts = format.parse(dateStr)
        if (ts != null) {
            return ts.time
        }
    } catch (pe: ParseException) {
        try {
            format.applyPattern(DATE_PATTERN1)
            val ts = format.parse(dateStr)
            if (ts != null) {
                return ts.time
            }
        } catch (pe1: ParseException) {
            format.applyPattern(DATE_PATTERN2)
            val ts = format.parse(dateStr)
            if (ts != null) {
                return ts.time
            }
        }
    }
    return null
}