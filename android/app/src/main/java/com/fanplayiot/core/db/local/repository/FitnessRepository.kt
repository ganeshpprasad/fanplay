package com.fanplayiot.core.db.local.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fanplayiot.core.db.local.FanplayiotDatabase
import com.fanplayiot.core.db.local.FanplayiotTemp.Companion.getTempDatabase
import com.fanplayiot.core.db.local.dao.FitnessDao
import com.fanplayiot.core.db.local.entity.*
import com.fanplayiot.core.db.local.entity.json.FitnessChallenge
import com.fanplayiot.core.remote.pojo.FanFitScore
import com.fanplayiot.core.remote.repository.FanFitRepository
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*

class FitnessRepository(val context: Context) {

    companion object {
        private const val TAG = "FitnessRepository"
        const val QUERY_SCD = 1
        const val QUERY_HR = 2
        const val QUERY_BP = 3
    }

    private val fitnessDao: FitnessDao
    private val scdPageCache = mutableSetOf<Int>()
    private val hrPageCache = mutableSetOf<Int>()
    private val bpPageCache = mutableSetOf<Int>()
    private val fitRepo = FanFitRepository(context, this)

    var fitnessSCDLive: LiveData<FitnessSCD>
    var fitnessHRLive: LiveData<FitnessHR>
    var fitnessBPLive: LiveData<FitnessBP>
    var fanDataLive: LiveData<FanData?>
    var modeLive: LiveData<Long>

    private val mFanFitScore = MutableLiveData<FanFitScore>(null)
    val fanFitScoreLive: LiveData<FanFitScore>
        get() = mFanFitScore

    init {
        val db = FanplayiotDatabase.getDatabase(context)
        fitnessDao = db.fitnessDao()
        fanDataLive = db.let {
            val fanEngageDao = db.dao()
            fanEngageDao.getFanData()
        } ?: run {
            MutableLiveData(null)
        }
        modeLive = fitnessDao.getFanDataModeLive()
        fitnessSCDLive = fitnessDao.geSCDLive()
        fitnessHRLive = fitnessDao.getLatestFitnessHR()
        fitnessBPLive = fitnessDao.getLatestFitnessBP()

    }

    suspend fun getAllFitnessForPage(user: User, pageId: Int) = withContext(Dispatchers.IO) {
        if (!scdPageCache.contains(pageId)) {
            getFitnessSCDForPage(user, pageId)
            scdPageCache.add(pageId)
        }
        if (!hrPageCache.contains(pageId)) {
            getFitnessHRForPage(user, pageId)
            hrPageCache.add(pageId)
        }
        if (!bpPageCache.contains(pageId)) {
            getFitnessBPForPage(user, pageId)
            bpPageCache.add(pageId)
        }
    }

    fun getAllFitnessSCD(dateRange: DateRange, scope: CoroutineScope): LiveData<List<StepDuration>> {
        val cal = Calendar.getInstance()
        val calnow = Calendar.getInstance()
        val groupPageId = dateRange.page
        val year: Int = cal.get(Calendar.YEAR)
        val month: Int = cal.get(Calendar.MONTH)
        val day: Int = cal.get(Calendar.DATE)

        cal.set(year, month, day, 0, 0, 0)
        calnow.set(year, month, day, 23, 59, 59)
        return when (dateRange.groupBy) {
            GROUP_BY_HOURS -> {
                if (dateRange.startDate != null && dateRange.endDate != null) {
                    cal.time = dateRange.startDate!!
                    calnow.time = dateRange.endDate!!
                } else {
                    cal.add(Calendar.DAY_OF_YEAR, -(groupPageId - 1))
                    calnow.add(Calendar.DAY_OF_YEAR, -(groupPageId - 1))
                }
                val page = findPageId(cal.time)
                scope.launch(Dispatchers.IO) {
                    val user = fitnessDao.getUserData() ?: return@launch
                    if (!scdPageCache.contains(page)) {
                        getFitnessSCDForPage(user, page)
                        scdPageCache.add(page)
                    }
                }
                fitnessDao.getAllFitnessSCD(cal.time.time, calnow.time.time)
            }
            GROUP_BY_WEEK -> {
                if (dateRange.startDate != null && dateRange.endDate != null) {
                    cal.time = dateRange.startDate!!
                    calnow.time = dateRange.endDate!!
                } else {
                    cal.add(Calendar.WEEK_OF_YEAR, -groupPageId)
                    calnow.add(Calendar.WEEK_OF_YEAR, -(groupPageId - 1))
                }
                val page = findPageId(cal.time)
                scope.launch(Dispatchers.IO) {
                    val user = fitnessDao.getUserData() ?: return@launch
                    if (!scdPageCache.contains(page)) {
                        getFitnessSCDForPage(user, page)
                        scdPageCache.add(page)
                    }
                }
                fitnessDao.getWeekFitnessSCD(cal.time.time, calnow.time.time)
            }
            GROUP_BY_MONTH -> {
                //cal.add(Calendar.MONTH, - groupPageId)
                //calnow.add(Calendar.MONTH, - (groupPageId - 1))
                if (dateRange.startDate != null && dateRange.endDate != null) {
                    cal.time = dateRange.startDate!!
                    calnow.time = dateRange.endDate!!
                } else {
                    cal.add(Calendar.MONTH, -(groupPageId * 6))
                    calnow.add(Calendar.MONTH, -((groupPageId) - 1) * 6)
                }
                val page = findPageId(cal.time)
                scope.launch(Dispatchers.IO) {
                    val user = fitnessDao.getUserData() ?: return@launch
                    if (page > 2) {
                        if (!scdPageCache.contains(page - 1)) {
                            getFitnessSCDForPage(user, page - 1)
                            scdPageCache.add(page - 1)
                        }
                    }
                    if (!scdPageCache.contains(page)) {
                        getFitnessSCDForPage(user, page)
                        scdPageCache.add(page)
                    }
                }
                fitnessDao.getMonthFitnessSCD(cal.time.time, calnow.time.time)
            }
            else -> {
                cal.add(Calendar.DAY_OF_YEAR, -1)
                fitnessDao.getAllFitnessSCD(cal.time.time, calnow.time.time)
            }
        }
    }

    fun getAllFitnessHR(dateRange: DateRange, scope: CoroutineScope): LiveData<List<HrDuration>> {
        val cal = Calendar.getInstance()
        val calnow = Calendar.getInstance()
        val groupPageId = dateRange.page
        val year: Int = cal.get(Calendar.YEAR)
        val month: Int = cal.get(Calendar.MONTH)
        val day: Int = cal.get(Calendar.DATE)

        cal.set(year, month, day, 0, 0, 0)
        calnow.set(year, month, day, 23, 59, 59)
        return when (dateRange.groupBy) {
            GROUP_BY_HOURS -> {
                if (dateRange.startDate != null && dateRange.endDate != null) {
                    cal.time = dateRange.startDate!!
                    calnow.time = dateRange.endDate!!
                } else {
                    cal.add(Calendar.DAY_OF_YEAR, -(groupPageId - 1))
                    calnow.add(Calendar.DAY_OF_YEAR, -(groupPageId - 1))
                }
                val page = findPageId(cal.time)
                scope.launch(Dispatchers.IO) {
                    val user = fitnessDao.getUserData() ?: return@launch
                    if (!hrPageCache.contains(page)) {
                        getFitnessHRForPage(user, page)
                        hrPageCache.add(page)
                    }
                }
                fitnessDao.getAllFitnessHR(cal.time.time, calnow.time.time)
            }
            GROUP_BY_WEEK -> {
                if (dateRange.startDate != null && dateRange.endDate != null) {
                    cal.time = dateRange.startDate!!
                    calnow.time = dateRange.endDate!!
                } else {
                    cal.add(Calendar.WEEK_OF_YEAR, -groupPageId)
                    calnow.add(Calendar.WEEK_OF_YEAR, -(groupPageId - 1))
                }
                val page = findPageId(cal.time)
                scope.launch(Dispatchers.IO) {
                    val user = fitnessDao.getUserData() ?: return@launch
                    if (!hrPageCache.contains(page)) {
                        getFitnessHRForPage(user, page)
                        hrPageCache.add(page)
                    }
                }
                fitnessDao.getWeekFitnessHR(cal.time.time, calnow.time.time)
            }
            GROUP_BY_MONTH -> {
                //cal.add(Calendar.MONTH, - groupPageId)
                //calnow.add(Calendar.MONTH, - (groupPageId - 1))
                if (dateRange.startDate != null && dateRange.endDate != null) {
                    cal.time = dateRange.startDate!!
                    calnow.time = dateRange.endDate!!
                } else {
                    cal.add(Calendar.MONTH, -(groupPageId * 6))
                    calnow.add(Calendar.MONTH, -((groupPageId) - 1) * 6)
                }
                val page = findPageId(cal.time)
                scope.launch(Dispatchers.IO) {
                    val user = fitnessDao.getUserData() ?: return@launch
                    if (page > 2) {
                        if (!hrPageCache.contains(page - 1)) {
                            getFitnessHRForPage(user, page - 1)
                            hrPageCache.add(page - 1)
                        }
                    }
                    if (!hrPageCache.contains(page)) {
                        getFitnessHRForPage(user, page)
                        hrPageCache.add(page)
                    }
                }
                fitnessDao.getMonthFitnessHR(cal.time.time, calnow.time.time)
            }
            else -> {
                cal.add(Calendar.DAY_OF_YEAR, -1)
                fitnessDao.getAllFitnessHR(cal.time.time, calnow.time.time)
            }
        }
    }

    fun getAllFitnessBP(dateRange: DateRange, scope: CoroutineScope): LiveData<List<BpDuration>> {
        val cal = Calendar.getInstance()
        val calnow = Calendar.getInstance()
        val groupPageId = dateRange.page
        val year: Int = cal.get(Calendar.YEAR)
        val month: Int = cal.get(Calendar.MONTH)
        val day: Int = cal.get(Calendar.DATE)

        cal.set(year, month, day, 0, 0, 0)
        calnow.set(year, month, day, 23, 59, 59)
        return when (dateRange.groupBy) {
            GROUP_BY_HOURS -> {
                if (dateRange.startDate != null && dateRange.endDate != null) {
                    cal.time = dateRange.startDate!!
                    calnow.time = dateRange.endDate!!
                } else {
                    cal.add(Calendar.DAY_OF_YEAR, -(groupPageId - 1))
                    calnow.add(Calendar.DAY_OF_YEAR, -(groupPageId - 1))
                }
                val page = findPageId(cal.time)
                scope.launch(Dispatchers.IO) {
                    val user = fitnessDao.getUserData() ?: return@launch
                    if (!bpPageCache.contains(page)) {
                        getFitnessBPForPage(user, page)
                        bpPageCache.add(page)
                    }
                }
                fitnessDao.getAllFitnessBP(cal.time.time, calnow.time.time)
            }
            GROUP_BY_WEEK -> {
                if (dateRange.startDate != null && dateRange.endDate != null) {
                    cal.time = dateRange.startDate!!
                    calnow.time = dateRange.endDate!!
                } else {
                    cal.add(Calendar.WEEK_OF_YEAR, -groupPageId)
                    calnow.add(Calendar.WEEK_OF_YEAR, -(groupPageId - 1))
                }
                val page = findPageId(cal.time)
                scope.launch(Dispatchers.IO) {
                    val user = fitnessDao.getUserData() ?: return@launch
                    if (!bpPageCache.contains(page)) {
                        getFitnessBPForPage(user, page)
                        bpPageCache.add(page)
                    }
                }
                fitnessDao.getWeekFitnessBP(cal.time.time, calnow.time.time)
            }
            GROUP_BY_MONTH -> {
                //cal.add(Calendar.MONTH, - groupPageId)
                //calnow.add(Calendar.MONTH, - (groupPageId - 1))
                if (dateRange.startDate != null && dateRange.endDate != null) {
                    cal.time = dateRange.startDate!!
                    calnow.time = dateRange.endDate!!
                } else {
                    cal.add(Calendar.MONTH, -(groupPageId * 6))
                    calnow.add(Calendar.MONTH, -((groupPageId) - 1) * 6)
                }
                val page = findPageId(cal.time)
                scope.launch(Dispatchers.IO) {
                    val user = fitnessDao.getUserData() ?: return@launch
                    if (page > 2) {
                        if (!bpPageCache.contains(page - 1)) {
                            getFitnessBPForPage(user, page - 1)
                            bpPageCache.add(page - 1)
                        }
                    }
                    if (!bpPageCache.contains(page)) {
                        getFitnessBPForPage(user, page)
                        bpPageCache.add(page)
                    }
                }

                fitnessDao.getMonthFitnessBP(cal.time.time, calnow.time.time)
            }
            else -> {
                cal.add(Calendar.DAY_OF_YEAR, -1)
                fitnessDao.getAllFitnessBP(cal.time.time, calnow.time.time)
            }
        }
    }

    suspend fun insertSCDForType(fitnessSCD: FitnessSCD, type: Int): Long {
        return if (type == PHONE) {
            fitnessDao.insert(fitnessSCD)
        } else {
            fitnessDao.insertSteps(fitnessSCD, type)
        }
        /*else if (type == DEVICE_BAND) {
            fitnessDao.insertSteps(fitnessSCD)
        }*/
    }

    suspend fun insertSCD(fitnessSCD: FitnessSCD, type: Int) {
        fitnessDao.insertSteps(fitnessSCD, type)
    }

    suspend fun updateSCDForBand(fitnessSCD: FitnessSCD, type: Int) {
        fitnessDao.updateSteps(fitnessSCD, type)
    }

    suspend fun insertHR(fitnessHR: FitnessHR) {
        fitnessDao.run {
            val available = findFitnessHR(fitnessHR.heartRate, fitnessHR.lastUpdated, fitnessHR.deviceType)
            if (available == null) insert(fitnessHR)
        }
    }

    suspend fun insertBP(fitnessBP: FitnessBP) {
        fitnessDao.run {
            val available = findFitnessBP(fitnessBP.systolic, fitnessBP.diastolic,
                    fitnessBP.lastUpdated, fitnessBP.deviceType)
            if (available == null) insert(fitnessBP)
        }
    }

    suspend fun insertSedentary(sedentary: Sedentary) {
        fitnessDao.insert(sedentary)
    }

    suspend fun insertStateSteps(stateSteps: StateSteps) {
        fitnessDao.insert(stateSteps)
    }

    suspend fun getFanFitScores() {
        fitnessDao.let {
            it.getUserData()?.let { user ->
                fitRepo.getFanFitScores(user)
            }
        }
    }

    suspend fun postFitness() {
        withContext(Dispatchers.IO) {
            try {
                // Check user not null
                val user = fitnessDao.getUserData() ?: return@withContext

                // Check if user session (challenge session etc) is running and active
                fitnessDao.getStartedSessionId()?.run {
                    Log.d(TAG, "active session $this so post fitness is skipped")
                    if (this > 0L) return@withContext
                }

                // POST for Fan Band
                var deviceType = DEVICE_BAND
                var scdList = fitnessDao.getAllNotSyncedSCD(deviceType)
                var hrList = fitnessDao.getAllNotSyncedHR(deviceType)
                var bpList = fitnessDao.getAllNotSyncedBP(deviceType)
                if ((scdList.size + hrList.size + bpList.size) > 0) {
                    fitRepo.postFitness(user, scdList, hrList, bpList, deviceType, this)
                }

                // POST for Google Fit
                deviceType = GOOGLE_FIT
                scdList = fitnessDao.getAllNotSyncedSCD(deviceType)
                hrList = fitnessDao.getAllNotSyncedHR(deviceType)
                bpList = fitnessDao.getAllNotSyncedBP(deviceType)
                if ((scdList.size + hrList.size + bpList.size) > 0) {
                    fitRepo.postFitness(user, scdList, hrList, bpList, deviceType, this)
                }

                // POST for Phone / camera
                deviceType = PHONE
                scdList = fitnessDao.getAllNotSyncedSCD(deviceType)
                hrList = fitnessDao.getAllNotSyncedHR(deviceType)
                bpList = fitnessDao.getAllNotSyncedBP(deviceType)
                if ((scdList.size + hrList.size + bpList.size) > 0) {
                    fitRepo.postFitness(user, scdList, hrList, bpList, deviceType, this)
                }

                fitRepo.getFanFitScores(user)
            } catch (e: Exception) {
                Log.e(TAG, "error in post Fitness", e)
            }
        }
    }

    fun getFitnessSCDForPage(user: User, pageId: Int) {
        fitRepo.getAllFitness(user, QUERY_SCD, pageId)
    }

    fun getFitnessHRForPage(user: User, pageId: Int) {
        fitRepo.getAllFitness(user, QUERY_HR, pageId)
    }

    fun getFitnessBPForPage(user: User, pageId: Int) {
        fitRepo.getAllFitness(user, QUERY_BP, pageId)
    }

    fun onSuccessPostFitness(scdList: List<FitnessSCD?>, hrList: List<FitnessHR?>, bpList: List<FitnessBP?>,
                             scope: CoroutineScope) {

        scope.launch(Dispatchers.IO) {
            try {
                val lastSynced = System.currentTimeMillis()
                scdList.forEach { scd ->
                    scd?.let {
                        it.lastSynced = lastSynced
                        fitnessDao.update(it)
                    }
                }
                hrList.forEach { hr ->
                    hr?.let {
                        it.lastSynced = lastSynced
                        fitnessDao.update(it)
                    }
                }
                bpList.forEach { bp ->
                    bp?.let {
                        it.lastSynced = lastSynced
                        fitnessDao.update(it)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "error in post Fitness", e)
            }
        }
    }

    fun onSuccessGetSCD(scdList: List<FitnessSCD>) {
        FanplayiotDatabase.databaseWriteExecutor.execute {
            try {
                fitnessDao.insertSCDAllUnique(scdList)
            } catch (e: Exception) {
                Log.e(TAG, "error in post Fitness", e)
            }
        }
    }

    fun onSuccessGetHR(hrList: List<FitnessHR>) {
        FanplayiotDatabase.databaseWriteExecutor.execute {
            try {
                fitnessDao.insertHRAllUnique(hrList)
            } catch (e: Exception) {
                Log.e(TAG, "error in post Fitness", e)
            }
        }
    }

    fun onSuccessGetBP(bpList: List<FitnessBP>) {
        FanplayiotDatabase.databaseWriteExecutor.execute {
            try {
                fitnessDao.insertBPAllUnique(bpList)
            } catch (e: Exception) {
                Log.e(TAG, "error in post Fitness", e)
            }
        }
    }

    private fun findPageId(start: Date): Int {
        var pageId = 1
        // Today
        val pageStart = Calendar.getInstance()
        val pageYear = pageStart.get(Calendar.YEAR)
        val pageMonth = pageStart.get(Calendar.MONTH)
        val pageDay = pageStart.get(Calendar.DATE)
        pageStart.set(pageYear, pageMonth, pageDay, 0, 0, 0)
        pageStart.add(Calendar.MONTH, -(pageId * 3))
        while (start < pageStart.time) {
            pageId++
            pageStart.set(pageYear, pageMonth, pageDay, 0, 0, 0)
            pageStart.add(Calendar.MONTH, -(pageId * 3))
        }

        return pageId
    }

    fun updateFanFitScore(fanFitScore: FanFitScore) {
        mFanFitScore.postValue(fanFitScore)
    }

    fun getFanSocialRepository(): FanSocialRepository {
        val tempDb = getTempDatabase(context)
        return FanSocialRepository(fitnessDao, tempDb.dao())
    }

    fun updateSessionId(id: Long, common: FitnessChallenge) {
        FanplayiotDatabase.databaseWriteExecutor.execute {
            fitnessDao.getFitnessActivityQueryId(id)?.let {
                it.commonJson = Json.encodeToString(common)
                it.lastSynced = System.currentTimeMillis()
                fitnessDao.updateFitnessActivity(it)
                Log.d(TAG, "session Id from server updated")
            }
        }
    }

}

data class DateRange(var groupBy: Int, var page: Int, var startDate: Date? = null, var endDate: Date? = null) {
    constructor(groupBy: Int, page: Int) : this(groupBy, page, null, null) {

    }
}