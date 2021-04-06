package com.fanplayiot.core.db.local.entity

import androidx.annotation.IntDef
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

@Entity
class Device(
        @field:PrimaryKey
        val address: String,

        @get:DeviceType
        @param:DeviceType
        val type: Int,

        val lastSynced: Long) {

    val rowAsString: String
        get() = "$address,$type,$lastSynced"

    companion object {
        const val DEVICE_BAND = 1
        const val DEVICE_EMOTE = 2
    }
}

@Retention(RetentionPolicy.SOURCE)
@IntDef(Device.DEVICE_BAND, Device.DEVICE_EMOTE)
annotation class DeviceType