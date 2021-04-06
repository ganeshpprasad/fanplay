package com.fanplayiot.core.db.local.entity

import androidx.annotation.IntDef
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

@Entity
class FanData {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
    var totalTapCount = 0
    var fanMetric = 0f
    var totalPoints: Long = 0
    var flag: Long = 0

    @ColumnInfo(index = true)
    var playerId: Int? = null

    @ColumnInfo(index = true)
    var teamId: Int? = null
    var lastUpdated: Long = 0
    var lastSynced: Long = 0

    constructor() {}

    @Ignore
    constructor(fanMetric: Float, totalPoints: Long, playerId: Int, teamId: Int) {
        this.fanMetric = fanMetric
        this.totalPoints = totalPoints
        this.playerId = playerId
        this.teamId = teamId
        lastUpdated = System.currentTimeMillis()
    }

    @Ignore
    constructor(toCopy: FanData) {
        totalTapCount = toCopy.totalTapCount
        fanMetric = toCopy.fanMetric
        totalPoints = toCopy.totalPoints
        flag = toCopy.flag
        playerId = toCopy.playerId
        teamId = toCopy.teamId
        lastUpdated = toCopy.lastUpdated
        lastSynced = toCopy.lastSynced
    }

    val mode: Int
        get() = flag.toInt()
    val rowAsString: String
        get() = ("" + id + ", total tap " + totalTapCount
                + "," + fanMetric + "," + totalPoints + ","
                + "player Id " + playerId + ", team id " + teamId + ","
                + lastUpdated + ", flag" + flag)
}

@Entity
class HeartRate {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
    var heartRate = 0

    @get:HeartRateType
    var type = 0
    var lastUpdated: Long = 0

    @Ignore
    constructor() {
    }

    constructor(id: Int, heartRate: Int, @HeartRateType type: Int, lastUpdated: Long) {
        this.id = id
        this.heartRate = heartRate
        this.type = type
        this.lastUpdated = lastUpdated
    }

    val rowAsString: String
        get() = "" + id + "," + heartRate + "," + type +
                "," + lastUpdated

    companion object {
        const val DEVICE_BAND = 1
        const val GOOGLE_FIT = 2
        const val CAMERA = 3
    }
}

@Retention(RetentionPolicy.SOURCE)
@IntDef(HeartRate.DEVICE_BAND, HeartRate.GOOGLE_FIT, HeartRate.CAMERA)
annotation class HeartRateType

@Entity
class WaveData {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
    var waveCount = 0

    @get:WaveType
    var waveType = 0
    var lastUpdated: Long = 0

    @Ignore
    constructor() {
    }

    constructor(id: Int, waveCount: Int, @WaveType waveType: Int, lastUpdated: Long) {
        this.id = id
        this.waveCount = waveCount
        this.waveType = waveType
        this.lastUpdated = lastUpdated
    }

    @Ignore
    constructor(toCopy: WaveData) {
        waveCount = toCopy.waveCount
        waveType = toCopy.waveType
        lastUpdated = toCopy.lastUpdated
    }

    val rowAsString: String
        get() = "$id,$waveCount,$waveType,$lastUpdated"

    companion object {
        const val DEVICE_FLAG = 1
        const val PHONE = 2
    }
}

@Retention(RetentionPolicy.SOURCE)
@IntDef(WaveData.DEVICE_FLAG, WaveData.PHONE)
annotation class WaveType

@Entity
class WhistleData {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
    var whistleCount = 0
    var whistleEarned = 0
    var whistleRedeemed = 0

    @ColumnInfo(defaultValue = "1")
    var whistleType = 0
    var lastUpdated: Long = 0
}