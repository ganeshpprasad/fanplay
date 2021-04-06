package com.fanplayiot.core.db.local.dao

import android.util.Log
import androidx.annotation.NonNull
import androidx.lifecycle.LiveData
import androidx.room.*
import com.fanplayiot.core.db.local.entity.*
import com.fanplayiot.core.db.local.entity.json.UserOrderProfile
import com.fanplayiot.core.db.local.entity.json.UserSocialProfile
import kotlinx.coroutines.flow.Flow

@Dao
abstract class FitnessDao {
    companion object {
        private const val TAG = "FitnessDao"
    }

    // SCD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(fitnessSCD: FitnessSCD): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertSync(fitnessSCD: FitnessSCD)

    @Transaction
    open suspend fun insertSteps(fitnessSCD: FitnessSCD, deviceType: Int): Long {
        //if (getFitnessSCDForSteps(fitnessSCD.steps, fitnessSCD.lastUpdated) != null) return
        val todays = getTodaySCD(deviceType)
        // No previous SCD available insert fitnessSCD
        if (todays == null) {
            Log.d(TAG, "prev is null current " + fitnessSCD.steps)
            return insert(fitnessSCD)

        }

        // if prev SCD is available for current day
        // Calculate delta values and insert
        if (fitnessSCD.steps - todays.steps > 0) {

            Log.d(TAG, "prev " + todays.steps + " now " + fitnessSCD.steps)
            return insert(FitnessSCD(0, deviceType,
                    fitnessSCD.steps - todays.steps,
                    fitnessSCD.calories - todays.calories,
                    fitnessSCD.distance - todays.distance,
                    DISTANCE_UNIT_KM, fitnessSCD.steps, fitnessSCD.lastUpdated, 0L))
        } else {
            // steps cannot be less than prev SCD so do nothing
            Log.d(TAG, "same or less current " + fitnessSCD.steps)
        }
        return -1
    }

    @Transaction
    open suspend fun updateSteps(fitnessSCD: FitnessSCD, deviceType: Int) {
        val todays = getTodaySCD(deviceType)
        // No previous SCD available insert fitnessSCD
        if (todays == null) {
            Log.d(TAG, "prev is null current " + fitnessSCD.steps)
            insert(fitnessSCD)
            return
        }

        val lastSCD = getFitnessSCDByType(deviceType)
        // last 15 sec if there is any SCD recorded update it otherwise insert SCD
        if (lastSCD != null) {
            if ((fitnessSCD.lastUpdated - (lastSCD.lastSynced ?: 0L)) < 30000L && ((lastSCD.lastSynced ?: 0L) == 0L)) {
                if (fitnessSCD.steps - todays.steps > 0) {
                    update(FitnessSCD(
                            lastSCD.id, deviceType,
                            fitnessSCD.steps - todays.steps,
                            fitnessSCD.calories - todays.calories,
                            fitnessSCD.distance - todays.distance,
                            lastSCD.distanceUnit,
                            fitnessSCD.steps,
                            fitnessSCD.lastUpdated, 0L
                    ))}
            } else {
                if (fitnessSCD.steps - todays.steps > 0) {
                    insert(FitnessSCD(0, deviceType,
                            fitnessSCD.steps - todays.steps,
                            fitnessSCD.calories - todays.calories,
                            fitnessSCD.distance - todays.distance,
                            DISTANCE_UNIT_KM, fitnessSCD.steps, fitnessSCD.lastUpdated, 0L))
                }
            }
        } else {
            if (fitnessSCD.steps - todays.steps > 0) {
                insert(FitnessSCD(0, deviceType,
                        fitnessSCD.steps - todays.steps,
                        fitnessSCD.calories - todays.calories,
                        fitnessSCD.distance - todays.distance,
                        DISTANCE_UNIT_KM, fitnessSCD.steps, fitnessSCD.lastUpdated, 0L))
            }
        }
    }

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract fun update(fitnessSCD: FitnessSCD)

    @Transaction
    open fun insertSCDAllUnique(@NonNull scdList: List<FitnessSCD>) {
        scdList.forEach { scd ->
            val temp = findFitnessSCD(scd.steps, scd.lastUpdated, scd.deviceType)
            if (temp == null) {
                insertSync(scd)
            }
        }
    }

    @Query("SELECT * FROM FitnessSCD WHERE steps = :steps AND datetime(lastUpdated/1000, 'unixepoch') = datetime(:lastUpdated/1000, 'unixepoch') AND deviceType = :deviceType ORDER BY lastUpdated DESC LIMIT 1")
    abstract fun findFitnessSCD(steps: Int, lastUpdated: Long, deviceType: Int): FitnessSCD?

    @Query("SELECT * FROM FitnessSCD ORDER BY lastUpdated DESC LIMIT 1")
    abstract fun getFitnessSCD(): FitnessSCD?

    @Query("SELECT * FROM FitnessSCD WHERE deviceType = :deviceType ORDER BY lastUpdated DESC LIMIT 1")
    abstract fun getFitnessSCDByType(deviceType: Int): FitnessSCD?

    @Query("SELECT SUM(steps) AS steps, SUM(calories) AS calories, SUM(distance) AS distance, 24 AS duration FROM FitnessSCD WHERE DATE(datetime(lastUpdated/1000, 'unixepoch')) = DATE('now') AND deviceType = :deviceType")
    abstract fun getTodaySCD(deviceType: Int): StepDuration?

    @Query("SELECT id, deviceType, SUM(steps) AS steps, SUM(calories) AS calories, SUM(distance) AS distance, distanceUnit, SUM(totalSteps) AS totalSteps, lastUpdated, lastSynced FROM FitnessSCD WHERE DATE(datetime(lastUpdated/1000, 'unixepoch')) = DATE('now')")
    abstract fun geSCDLive(): LiveData<FitnessSCD>

    //1604990000619 1604996966005
    @Query("SELECT * FROM FitnessSCD WHERE lastUpdated > :start AND lastUpdated <= :end LIMIT 1")
    abstract fun getFitnessSCDBetween(start: Long, end: Long): FitnessSCD?

    @Query("SELECT * FROM FitnessSCD ORDER BY lastUpdated DESC LIMIT 1")
    abstract fun getLatestFitnessSCD(): LiveData<FitnessSCD>

    //"SELECT (lastUpdated / 7200000) * 7200000 AS duration,  SUM(steps) AS steps, SUM(calories) AS calories, SUM(distance) AS distance FROM FitnessSCD WHERE lastUpdated > :start AND lastUpdated <= :end GROUP BY duration ORDER BY duration"
    @Query("SELECT (lastUpdated / 3600000) * 3600000 AS duration,  SUM(steps) AS steps, SUM(calories) AS calories, SUM(distance) AS distance FROM FitnessSCD WHERE lastUpdated > :start AND lastUpdated <= :end GROUP BY duration ORDER BY duration")
    abstract fun getAllFitnessSCD(start: Long, end: Long): LiveData<List<StepDuration>>

    @Query("SELECT (lastUpdated / 86400000) * 86400000 AS duration, SUM(steps) AS steps, SUM(calories) AS calories, SUM(distance) AS distance FROM FitnessSCD WHERE lastUpdated > :start AND lastUpdated <= :end GROUP BY duration ORDER BY duration")
    abstract fun getWeekFitnessSCD(start: Long, end: Long): LiveData<List<StepDuration>>

    //@Query("SELECT (lastUpdated / 2628002880) * 2628002880 AS duration, SUM(steps) AS steps, SUM(calories) AS calories, SUM(distance) AS distance FROM FitnessSCD WHERE lastUpdated > :start AND lastUpdated <= :end GROUP BY duration ORDER BY duration")
    @Query("SELECT STRFTIME('%m-%Y' , DATETIME(lastUpdated/1000, 'unixepoch')) AS duration, SUM(steps) AS steps, SUM(calories) AS calories, SUM(distance) AS distance FROM FitnessSCD WHERE lastUpdated > :start AND lastUpdated <= :end GROUP BY duration ORDER BY duration")
    abstract fun getMonthFitnessSCD(start: Long, end: Long): LiveData<List<StepDuration>>

    @Query("SELECT * FROM FitnessSCD WHERE lastSynced = 0 AND deviceType = :deviceType")
    abstract fun getAllNotSyncedSCD(deviceType: Int): List<FitnessSCD>

    // HR
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(fitnessHR: FitnessHR)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertSync(fitnessHR: FitnessHR)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract fun update(fitnessHR: FitnessHR)

    @Transaction
    open fun insertHRAllUnique(@NonNull hrList: List<FitnessHR>) {
        hrList.forEach { hr ->
            val temp = findFitnessHR(hr.heartRate, hr.lastUpdated, hr.deviceType)
            if (temp == null) {
                insertSync(hr)
            }
        }
    }

    @Query("SELECT * FROM FitnessHR WHERE heartRate = :heartRate AND datetime(lastUpdated/1000, 'unixepoch') = datetime(:lastUpdated/1000, 'unixepoch') AND deviceType = :deviceType ORDER BY lastUpdated DESC LIMIT 1")
    abstract fun findFitnessHR(heartRate: Int, lastUpdated: Long, deviceType: Int): FitnessHR?

    @Query("SELECT * FROM FitnessHR ORDER BY lastUpdated DESC LIMIT 1")
    abstract fun getLatestFitnessHR(): LiveData<FitnessHR>

    @Query("SELECT * FROM FitnessHR WHERE lastSynced = 0 AND deviceType = :deviceType")
    abstract fun getAllNotSyncedHR(deviceType: Int): List<FitnessHR>

    // "SELECT lastUpdated AS duration, heartRate AS heartRate FROM FitnessHR WHERE lastUpdated > :start AND lastUpdated <= :end GROUP BY duration ORDER BY duration DESC"
    // "SELECT (lastUpdated / 3600000) * 3600000 AS duration, AVG(heartRate) AS heartRate FROM FitnessHR WHERE lastUpdated > :start AND lastUpdated <= :end GROUP BY duration ORDER BY duration"
    @Query("SELECT lastUpdated AS duration, heartRate AS heartRate FROM FitnessHR WHERE lastUpdated > :start AND lastUpdated <= :end GROUP BY duration ORDER BY duration")
    abstract fun getAllFitnessHR(start: Long, end: Long): LiveData<List<HrDuration>>

    @Query("SELECT (lastUpdated / 86400000) * 86400000 AS duration, AVG(heartRate) AS heartRate FROM FitnessHR WHERE lastUpdated > :start AND lastUpdated <= :end GROUP BY duration ORDER BY duration")
    abstract fun getWeekFitnessHR(start: Long, end: Long): LiveData<List<HrDuration>>

    //"SELECT STRFTIME('%m-%Y' , DATETIME(lastUpdated/1000, 'unixepoch')) AS duration, AVG(heartRate) AS heartRate FROM FitnessHR WHERE lastUpdated > :start AND lastUpdated <= :end GROUP BY duration ORDER BY duration"
    @Query("SELECT (lastUpdated / 86400000) * 86400000 AS duration, AVG(heartRate) AS heartRate FROM FitnessHR WHERE lastUpdated > :start AND lastUpdated <= :end GROUP BY duration ORDER BY duration")
    abstract fun getMonthFitnessHR(start: Long, end: Long): LiveData<List<HrDuration>>

    // BP
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(fitnessBP: FitnessBP)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertSync(fitnessBP: FitnessBP)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract fun update(fitnessBP: FitnessBP)

    @Transaction
    open fun insertBPAllUnique(@NonNull bpList: List<FitnessBP>) {
        bpList.forEach { bp ->
            val temp = findFitnessBP(bp.systolic, bp.diastolic, bp.lastUpdated, bp.deviceType)
            if (temp == null) {
                insertSync(bp)
            }
        }
    }

    @Query("SELECT * FROM FitnessBP WHERE systolic = :systolic AND diastolic = :diastolic AND datetime(lastUpdated/1000, 'unixepoch') = datetime(:lastUpdated/1000, 'unixepoch') AND deviceType = :deviceType ORDER BY lastUpdated DESC LIMIT 1")
    abstract fun findFitnessBP(systolic: Int, diastolic: Int, lastUpdated: Long, deviceType: Int): FitnessBP?

    @Query("SELECT * FROM FitnessBP ORDER BY lastUpdated DESC LIMIT 1")
    abstract fun getLatestFitnessBP(): LiveData<FitnessBP>

    @Query("SELECT * FROM FitnessBP WHERE lastSynced = 0 AND deviceType = :deviceType")
    abstract fun getAllNotSyncedBP(deviceType: Int): List<FitnessBP>

    // "SELECT (lastUpdated / 7200000) * 7200000 AS duration, AVG(systolic) AS systolic, AVG(diastolic) AS diastolic FROM FitnessBP WHERE lastUpdated > :start AND lastUpdated <= :end GROUP BY duration ORDER BY duration"
    @Query("SELECT lastUpdated AS duration, systolic, diastolic FROM FitnessBP WHERE lastUpdated > :start AND lastUpdated <= :end ORDER BY duration DESC")
    abstract fun getAllFitnessBP(start: Long, end: Long): LiveData<List<BpDuration>>

    @Query("SELECT (lastUpdated / 86400000) * 86400000 AS duration, AVG(systolic) AS systolic, AVG(diastolic) AS diastolic FROM FitnessBP WHERE lastUpdated > :start AND lastUpdated <= :end GROUP BY duration ORDER BY duration")
    abstract fun getWeekFitnessBP(start: Long, end: Long): LiveData<List<BpDuration>>

    @Query("SELECT STRFTIME('%m-%Y' , DATETIME(lastUpdated/1000, 'unixepoch')) AS duration, AVG(systolic) AS systolic, AVG(diastolic) AS diastolic FROM FitnessBP WHERE lastUpdated > :start AND lastUpdated <= :end GROUP BY duration ORDER BY duration")
    abstract fun getMonthFitnessBP(start: Long, end: Long): LiveData<List<BpDuration>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(sedentary: com.fanplayiot.core.db.local.entity.Sedentary)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(stateSteps: com.fanplayiot.core.db.local.entity.StateSteps)

    @Query("SELECT * FROM User WHERE id = 1")
    abstract suspend fun getUserData(): com.fanplayiot.core.db.local.entity.User?

    @Query("SELECT teamIdServer FROM Team ORDER BY id LIMIT 1 ")
    abstract suspend fun getTeamIdServer(): Long?

    @Query("SELECT sid, profileName, profileImgUrl, tokenId, teamIdServer, -1 AS affiliationId  FROM User u, TEAM t WHERE u.id = 1 AND t.id = 1")
    abstract fun getUserSocialProfile(): Flow<UserSocialProfile?>

    @Query("SELECT sid, profileName, email, mobile  FROM User WHERE id = 1 ")
    abstract suspend fun getUserOrderProfile(): UserOrderProfile?

    @Query("SELECT flag FROM FanData ORDER BY lastUpdated DESC LIMIT 1")
    abstract fun getFanDataModeLive(): LiveData<Long>

    @Query("SELECT flag FROM FanData ORDER BY lastUpdated DESC LIMIT 1")
    abstract suspend fun getMode(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(fitnessActivity: FitnessActivity): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun update(fitnessActivity: FitnessActivity)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract fun updateFitnessActivity(fitnessActivity: FitnessActivity)

    @Delete
    abstract suspend fun delete(fitnessActivity: FitnessActivity)

    @Query("SELECT * FROM FitnessActivity WHERE id = :id")
    abstract fun getFitnessActivityQueryId(id: Long): FitnessActivity?

    @Query("SELECT * FROM FitnessActivity WHERE id = :id")
    abstract suspend fun getFitnessActivityForId(id: Long): FitnessActivity?

    @Query("SELECT activitySCD, activityHR, activityBP FROM FitnessActivity")
    abstract suspend fun getAllFitnessFromActivity(): List<SessionFitnessIds?>

    @Query("SELECT id FROM FitnessActivity WHERE `end` == 0 AND lastSynced == 0 ORDER BY start DESC LIMIT 1")
    abstract suspend fun getStartedSessionId(): Long?

    @Query("SELECT id FROM FitnessActivity WHERE `end` == 0 AND lastSynced == 0 ORDER BY start DESC LIMIT 1")
    abstract fun getStartedSession(): Flow<Long?>

    @Query("SELECT * FROM FitnessActivity WHERE id = :id")
    abstract fun getFitnessActivityForIdAsFlow(id: Long): Flow<FitnessActivity>

    @Query("SELECT * FROM FitnessSCD WHERE lastUpdated > :start AND lastUpdated <= :end")
    abstract suspend fun getFitnessSCDForRange(start: Long, end: Long): List<FitnessSCD>

    @Query("SELECT * FROM FitnessHR WHERE lastUpdated > :start AND lastUpdated <= :end")
    abstract suspend fun getFitnessHRForRange(start: Long, end: Long): List<FitnessHR>

    @Query("SELECT * FROM FitnessBP WHERE lastUpdated > :start AND lastUpdated <= :end")
    abstract suspend fun getFitnessBPForRange(start: Long, end: Long): List<FitnessBP>

}