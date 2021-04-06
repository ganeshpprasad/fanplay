package com.fanplayiot.core.db.local.repository

import android.content.Context
import android.content.SharedPreferences
import com.fanplayiot.core.utils.Constant
import com.mcube.ms.sdk.definitions.MSDefinition

class FeatureStorage(context: Context) {
    private val sp: SharedPreferences = context.getSharedPreferences(Constant.PREF_FILE_KEY, Context.MODE_PRIVATE)
    fun updateSedentary(sedentary: Int) {
        sp.edit().putInt(Constant.SEDENTARY_KEY, sedentary).apply()
    }

    val sedentary: Int
        get() = sp.getInt(Constant.SEDENTARY_KEY, MSDefinition.SEDENTARY_DEFAULT_MINS)

    fun updateAlarm(alarmJson: String) {
        sp.edit().putString(Constant.ALARM_KEY, alarmJson).apply()
    }

    val alarm: String
        get() = sp.getString(Constant.ALARM_KEY, "") ?: ""

    private fun updateNotifySettings(notifySettings: Long) {
        sp.edit().putLong(Constant.NOTIFY_KEY, notifySettings).apply()
    }

    private val notifySettings: Long
        get() = sp.getLong(Constant.NOTIFY_KEY, 0L)

    fun updateOtherMsgNotify(defId: Int, enable: Boolean) {
        if (enable) updateNotifySettings(notifySettings or (1L shl defId))
        else updateNotifySettings(notifySettings and (1L shl defId).inv())
    }

    fun isEnabledNotify(defId: Int): Boolean {
        return notifySettings shr defId and 1L == 1L
    }

    private fun updateDisplaySettings(displaySetting: Long) {
        sp.edit().putLong(Constant.DISPLAY_SETTINGS_KEY, displaySetting).apply()
    }

    private val displaySettings: Long
        get() = sp.getLong(Constant.DISPLAY_SETTINGS_KEY, 2047L) // 2047 Decimal Long is 011111111111 in Binary

    fun updateDisplay(argId: Int, enable: Boolean) {
        if (enable) updateDisplaySettings(displaySettings or (1L shl argId))
        else updateDisplaySettings(displaySettings and (1L shl argId).inv())
    }

    fun isEnabledDisplay(argId: Int): Boolean {
        return displaySettings shr argId and 1L == 1L
    }

}