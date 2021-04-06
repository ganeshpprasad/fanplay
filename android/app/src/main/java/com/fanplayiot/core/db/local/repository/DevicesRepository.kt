package com.fanplayiot.core.db.local.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.fanplayiot.core.db.local.FanplayiotDatabase.Companion.getDatabase
import com.fanplayiot.core.db.local.entity.Device

class DevicesRepository(context: Context) {
    private val dao: com.fanplayiot.core.db.local.dao.FanEngageDao
    @JvmField
    var bandLive: LiveData<Device?>
    @JvmField
    var emoteLive: LiveData<Device?>

    init {
        val db = getDatabase(context)
        dao = db.dao()
        bandLive = dao.getDevice(Device.DEVICE_BAND)
        emoteLive = dao.getDevice(Device.DEVICE_EMOTE)
    }

    fun insertDevice(device: Device) {
        com.fanplayiot.core.db.local.FanplayiotDatabase.databaseWriteExecutor.execute {
            try {
                dao.insert(device)
            } catch (e: Exception) {
                Log.e(TAG, "error ", e)
            }
        }
    }

    fun deleteDevice(deviceAddress: String) {
        com.fanplayiot.core.db.local.FanplayiotDatabase.databaseWriteExecutor.execute {
            try {
                dao.deleteDevice(deviceAddress)
            } catch (e: Exception) {
                Log.e(TAG, "error ", e)
            }
        }
    }

    companion object {
        private const val TAG = "DevicesRepository"
    }

}