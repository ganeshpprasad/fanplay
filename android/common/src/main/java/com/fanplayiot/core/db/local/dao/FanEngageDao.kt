package com.fanplayiot.core.db.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.fanplayiot.core.db.local.entity.*
import com.fanplayiot.core.db.local.dao.ScoreHelper

@Dao
abstract class FanEngageDao {
    // Device related
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(device: Device?)

    @Query("SELECT * FROM Device WHERE type = :type ORDER BY lastSynced LIMIT 1")
    abstract fun getDevice(@DeviceType type: Int): LiveData<Device?>

    @Query("DELETE FROM Device WHERE address = :address")
    abstract fun deleteDevice(address: String?)

    @get:Query("SELECT * FROM Device")
    abstract val allDevice: Array<Device?>?

    // Fan Data related
    @Insert(onConflict = OnConflictStrategy.REPLACE)

    abstract fun insert(fanData: FanData?)
    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract fun update(fanData: FanData?)

    @Query("SELECT * FROM FanData ORDER BY lastUpdated DESC LIMIT 1")
    abstract fun getFanData(): LiveData<FanData?>

    @Query("SELECT * FROM FanData")
    abstract fun getAllFanData(): Array<FanData?>

    @Query("DELETE FROM FanData WHERE id NOT IN (:idArray)")
    protected abstract fun deleteFanDataNotIn(idArray: IntArray?)

    @get:Query("SELECT * FROM FanData ORDER BY lastUpdated DESC LIMIT 2")
    protected abstract val lastTwoFanData: Array<FanData>?

    @get:Query("SELECT * FROM FanData ORDER BY lastUpdated DESC LIMIT 1")
    abstract val fanDataLatest: FanData?

    @Transaction
    open fun insertOrUpdate(fanData: FanData) {
        insert(fanData)
        val fanDataArray = lastTwoFanData ?: return
        val idArray = IntArray(2)
        idArray[0] = fanDataArray[0].id!!
        idArray[1] = if (fanDataArray.size == 2) fanDataArray[1].id!! else 0
        deleteFanDataNotIn(idArray)
    }

    // Heart rate related
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract fun insert(heartRate: HeartRate?): Long
    @Update(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun update(heartRate: HeartRate?)

    @Query("SELECT * FROM HeartRate ORDER BY lastUpdated DESC LIMIT 1")
    abstract fun getHeartRate(): LiveData<HeartRate?>

    @Query("SELECT * FROM HeartRate")
    abstract fun getAllHeartRate(): Array<HeartRate?>

    // Wave related
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(waveData: WaveData?)
    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract fun update(waveData: WaveData?)

    @Query("SELECT * FROM WaveData ORDER BY lastUpdated DESC LIMIT 1")
    abstract fun getWaveData(): LiveData<WaveData?>

    @Query("SELECT * FROM WaveData ORDER BY lastUpdated DESC LIMIT 1")
    abstract fun getWaveDataLatest(): WaveData

    @Query("SELECT * FROM WaveData")
    abstract fun getAllWaveData(): Array<WaveData?>

    @Query("DELETE FROM WaveData WHERE id NOT IN (:idArray)")
    protected abstract fun deleteWaveDataNotIn(idArray: IntArray?)

    @Query("SELECT * FROM WaveData ORDER BY lastUpdated DESC LIMIT 2")
    protected abstract fun getLastTwoWaveData(): Array<WaveData>?

    @Transaction
    open fun insertOrUpdate(waveData: WaveData) {
        insert(waveData)
        val waveDataArray = getLastTwoWaveData() ?: return
        val idArray = IntArray(2)
        idArray[0] = waveDataArray[0].id!!
        idArray[1] = if (waveDataArray.size == 2) waveDataArray[1].id!! else 0
        deleteWaveDataNotIn(idArray)
    }

    // Whistle related
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insert(whistleData: WhistleData?): Long?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract fun update(whistleData: WhistleData?)

    @Query("SELECT * FROM WhistleData ORDER BY lastUpdated DESC LIMIT 1")
    abstract fun getWhistleData(): LiveData<WhistleData?>

    @Query("SELECT * FROM WhistleData ORDER BY lastUpdated DESC LIMIT 1")
    abstract fun getWhistleDataLatest(): WhistleData?

    @Query("SELECT * FROM WhistleData ORDER BY id LIMIT 1")
    abstract fun getOneWhistleData(): WhistleData?

    @Query("DELETE FROM WhistleData WHERE id NOT IN (:idArray)")
    protected abstract fun deleteWhistleDataNotIn(idArray: IntArray?)

    @get:Query("SELECT * FROM WhistleData ORDER BY lastUpdated DESC LIMIT 2")
    protected abstract val lastTwoWhistleData: Array<WhistleData>?

    @Transaction
    open fun insertOrUpdate(whistleData: WhistleData?) {
        insert(whistleData)
        val whistleDataArray = lastTwoWhistleData ?: return
        val idArray = IntArray(2)
        idArray[0] = whistleDataArray[0].id!!
        idArray[1] = if (whistleDataArray.size == 2) whistleDataArray[1].id!! else 0
        deleteWhistleDataNotIn(idArray)
    }

    // For computing fan metric
    // Called when heart rate reading is completed
    @Transaction
    open fun insertAndCompute(heartRate: HeartRate, age: Int, hrZone: Int, teamId: Int, playerImageId: Int): FanData? {
        try {
            val hrId = insert(heartRate)
            if (hrId == -1L) {
                update(heartRate)
            }
        } catch (e: Exception) {
            return null
        }
        val fanDataArray = lastTwoFanData
        val waveDataArray = getLastTwoWaveData()
        val whistleDataArray = lastTwoWhistleData
        val lastUpdated = System.currentTimeMillis()

        // to insert new FanData row set teamId and playerId
        // Needed for computing delta value
        var fanData = FanData()
        fanData.teamId = teamId
        fanData.playerId = playerImageId

        // to insert new WaveData row set
        // Needed for computing delta value
        var waveData = WaveData()

        // to insert new WhistleData row set
        // Needed for computing delta value
        val whistleData = WhistleData()

        // Calculate delta values
        var tap1 = 0
        var tap2 = 0
        val tapDelta: Int
        var wave1 = 0
        var wave2 = 0
        val waveDelta: Int
        var whr1 = 0
        var whr2 = 0
        val whrDelta: Int
        val oldPoints: Long
        val newPoints: Long
        if (fanDataArray != null && fanDataArray.size == 2) {
            // last 2 Fan data available
            tap1 = fanDataArray[0].totalTapCount
            tap2 = fanDataArray[1].totalTapCount
            fanData = FanData(fanDataArray[0])
        }
        if (waveDataArray != null && waveDataArray.size == 2) {
            // last 2 Wave data available
            wave1 = waveDataArray[0].waveCount
            wave2 = waveDataArray[1].waveCount
            waveData = WaveData(waveDataArray[0])
        }
        if (whistleDataArray != null && whistleDataArray.size == 2) {
            // last 2 Whistle Redeemed data available
            whr1 = whistleDataArray[0].whistleRedeemed
            whr2 = whistleDataArray[1].whistleRedeemed
            whistleData.whistleCount = whistleDataArray[0].whistleCount
            whistleData.whistleEarned = whistleDataArray[0].whistleEarned
            whistleData.whistleRedeemed = whistleDataArray[0].whistleRedeemed
            whistleData.whistleType = whistleDataArray[0].whistleType
            whistleData.lastUpdated = whistleDataArray[0].lastUpdated
        }
        if (fanDataArray != null && fanDataArray.size == 1) {
            // only one Fan data available
            tap1 = fanDataArray[0].totalTapCount
            fanData = FanData(fanDataArray[0])
        }
        if (waveDataArray != null && waveDataArray.size == 1) {
            // only one Wave data available
            wave1 = waveDataArray[0].waveCount
            waveData = WaveData(waveDataArray[0])
        }
        if (whistleDataArray != null && whistleDataArray.size == 1) {
            whr1 = whistleDataArray[0].whistleRedeemed
            whistleData.whistleCount = whistleDataArray[0].whistleCount
            whistleData.whistleEarned = whistleDataArray[0].whistleEarned
            whistleData.whistleRedeemed = whistleDataArray[0].whistleRedeemed
            whistleData.whistleType = whistleDataArray[0].whistleType
            whistleData.lastUpdated = whistleDataArray[0].lastUpdated
        }
        oldPoints = fanData.totalPoints
        tapDelta = ScoreHelper.deltaOf(tap1, tap2) // delta of tap counts
        waveDelta = ScoreHelper.deltaOf(wave1, wave2) // delta of wave counts
        whrDelta = ScoreHelper.deltaOf(whr1, whr2) // delta of whistle redeemed counts
        val hr = heartRate.heartRate.toFloat() // heart rate
        val fanMetric = ScoreHelper.hrToPoints(hr) +
                ScoreHelper.deltaToPoints(tapDelta) +
                ScoreHelper.deltaToPoints(waveDelta)
        fanData.fanMetric = Math.round(fanMetric * 10.0f) / 10.0f
        newPoints = ScoreHelper.calcNewPoints(whrDelta, (fanMetric * 10).toLong(), hrZone)
        fanData.totalPoints = oldPoints + newPoints
        val whistleEarned = ((oldPoints + newPoints) / 100).toInt() // whistle earned = Total points / 100
        whistleData.whistleEarned = whistleEarned
        whistleData.whistleCount = Math.abs(whistleData.whistleRedeemed - whistleData.whistleEarned)
        whistleData.lastUpdated = lastUpdated
        insertOrUpdate(whistleData)
        fanData.lastUpdated = lastUpdated
        insertOrUpdate(fanData)
        insertOrUpdate(waveData)
        return fanData
    }
}