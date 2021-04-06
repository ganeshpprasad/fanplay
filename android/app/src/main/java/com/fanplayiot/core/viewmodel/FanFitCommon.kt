package com.fanplayiot.core.viewmodel

import android.app.Application
import android.bluetooth.BluetoothProfile
import android.util.Log
import androidx.lifecycle.*
import androidx.work.WorkManager
import com.fanplayiot.core.background.FitnessWork
import com.fanplayiot.core.bluetooth.ConnectionResult
import com.fanplayiot.core.bluetooth.SDKManager
import com.fanplayiot.core.db.local.entity.*
import com.fanplayiot.core.db.local.repository.DateRange
import com.fanplayiot.core.db.local.repository.FeatureStorage
import com.fanplayiot.core.db.local.repository.FitnessRepository
import com.fanplayiot.core.db.local.repository.UserProfileStorage
import com.fanplayiot.core.foreground.service.FanFitListener
import com.fanplayiot.core.googlefit.GoogleFitService
import com.fanplayiot.core.googlefit.HEART_RATE_TASK
import com.fanplayiot.core.googlefit.ServiceCallback
import com.fanplayiot.core.remote.FirebaseAuthService
import com.fanplayiot.core.remote.firebase.analytics.AnalyticsService
import com.fanplayiot.core.utils.OneTimeEvent
import com.fanplayiot.core.ui.camera.CameraUtils
import com.fanplayiot.core.ui.home.fanfit.FitnessViewModel
import com.fanplayiot.core.ui.home.fanfit.HrBpViewModel
import com.fanplayiot.core.ui.home.fanfit.SCDViewModel
import com.fanplayiot.core.viewmodel.MainProfileViewModel.FEET
import com.fanplayiot.core.viewmodel.MainProfileViewModel.LB
import com.mcube.ms.sdk.definitions.MSDefinition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

class FanFitCommon(application: Application, @JvmField val homeViewModel: HomeViewModel) : AndroidViewModel(application), ServiceCallback, FanFitListener {
    internal val repository: FitnessRepository = FitnessRepository(application)
    internal val sdk = SDKManager.instance(getApplication())
    val fanFitTab = MutableLiveData(DateRange(GROUP_BY_HOURS, 1))

    @JvmField
    val callRefresh = MutableLiveData<Boolean>(null)
    val startHeartRate = MutableLiveData<Int>(null)
    val stopHeartRate = MutableLiveData<Int>(null)
    var stopProgress = MutableLiveData(false)
    var startProgress = MutableLiveData(false)

    @JvmField
    val startedSession: LiveData<Long?>
    private val mDeviceReady = MutableLiveData(OneTimeEvent(false))
    val deviceReady: LiveData<OneTimeEvent<Boolean>>
        get() = mDeviceReady

    var hourCurrPage = 1
    var weekCurrPage = 1
    var monthCurrPage = 1
    var currAnalysisPos = 0
    private val profileStorage = UserProfileStorage(application)
    private val featureStorage = FeatureStorage(application)
    private val storage = UserProfileStorage(application)
    private val fanSocialRepository = repository.getFanSocialRepository()
    val userLive: LiveData<User> = profileStorage.userLive
    val modeLive: LiveData<Long> = repository.modeLive
    private lateinit var uuid: UUID

    private val mPedometerEnabled = MutableLiveData(false)
    val pedometerEnabled: LiveData<Boolean>
        get() = mPedometerEnabled

    private val mFirmwareVersion = MutableLiveData<String>(null)
    val firmwareVersion: LiveData<String>
        get() = mFirmwareVersion

    val service = GoogleFitService(this)

    init {
        WorkManager.getInstance(getApplication()).cancelAllWorkByTag(FitnessWork.TAG)
        /*if (FirebaseAuthService.currentUser() != null) {
            uuid = FitnessWork.startPeriodicFitnessSync(application, FitnessWork.DEFAULT_INTERVAL)
        }*/
        updateUserModule()
        startedSession = fanSocialRepository.getStartedSession()
                .asLiveData(viewModelScope.coroutineContext)
    }

    override fun onCleared() {
        super.onCleared()
        if (this::uuid.isInitialized) {
            WorkManager.getInstance(getApplication()).cancelAllWorkByTag(FitnessWork.TAG)
            FitnessWork.startFitnessSync(getApplication())
        }
    }

    fun setPedometerEnabled(isEnabled: Boolean) {
        mPedometerEnabled.value = isEnabled
    }

    fun setFirmwareVersion(version: String) {
        mFirmwareVersion.value = version
    }

    fun updateUserModule() {
        storage.updateUserModule()
    }

    fun getFirstPage(user: User) {
        viewModelScope.launch {
            repository.getAllFitnessForPage(user, 1)
        }
    }

    fun getGoal(user: User): Goal {
        return profileStorage.getProfile(user).goal
    }

    fun isConnected(): Boolean {
        modeLive.value?.let { mode ->
            return when {
                mode.toInt() == HeartRate.DEVICE_BAND -> {
                    sdk.ble.connectionState == BluetoothProfile.STATE_CONNECTED
                }
                mode.toInt() == HeartRate.GOOGLE_FIT -> {
                    service.checkPermissions(getApplication())
                }
                mode.toInt() == HeartRate.CAMERA -> {
                    CameraUtils.hasCameraPermission(getApplication())
                }
                else -> {
                    true
                }
            }
        }
        return true
    }

    fun requestSync() {

        viewModelScope.launch(Dispatchers.IO) {
            repository.getFanFitScores()
        }

        modeLive.value?.let { mode ->
            when (mode.toInt()) {
                HeartRate.DEVICE_BAND -> {
                    if (!isConnected()) {
                        //Log.d(TAG, "not connected")
                        return@requestSync
                    }
                    sdk.ble.requestSync()
                }
                HeartRate.CAMERA, HeartRate.GOOGLE_FIT -> {
                    callRefresh.postValue(true)
                    return@requestSync
                }
                else -> {
                    // do nothing
                }
            }
        }
        callRefresh.postValue(true)
    }

    fun insertSCD(steps: Int) {
        if (!isConnected()) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val strCalorie = sdk.calorie?.trim() ?: ""
                val strDistance = sdk.distance?.replace("km", "")?.trim() ?: ""
                if (strCalorie.isNotEmpty() && strDistance.isNotEmpty()) {
                    val scd = FitnessSCD(steps, strCalorie.toFloat(), strDistance.toFloat())
                    repository.insertSCD(scd, DEVICE_BAND)
                }
            } catch (e: Exception) {
                Log.e(TAG, "nfe", e)
            }
        }
    }

    fun updateSCDForBand(steps: Int) {
        if (!isConnected()) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val strCalorie = sdk.calorie?.trim() ?: ""
                val strDistance = sdk.distance?.replace("km", "")?.trim() ?: ""
                if (strCalorie.isNotEmpty() && strDistance.isNotEmpty()) {
                    val scd = FitnessSCD(steps, steps, strCalorie.toFloat(), strDistance.toFloat(),
                            System.currentTimeMillis())
                    //Log.d(TAG, "steps: ${steps} cal ${strCalorie}")
                    repository.updateSCDForBand(scd, DEVICE_BAND)
                }
            } catch (e: Exception) {
                Log.e(TAG, "nfe", e)
            }
        }
    }

    override fun insertHR(heartRate: Int, lastUpdated: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 0, DEVICE_BAND, heartRate, System.currentTimeMillis(), 0
                val fitnessHR = FitnessHR(0, DEVICE_BAND, heartRate, lastUpdated, 0L)
                repository.insertHR(fitnessHR)

            } catch (e: Exception) {
                Log.e(TAG, "nfe", e)
            }
        }
    }

    fun insertBP(systolic: Int, diastolic: Int, lastUpdated: Long = System.currentTimeMillis()) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                //0, DEVICE_BAND, systolic, diastolic, System.currentTimeMillis(), 0
                val fitnessBP = FitnessBP(0, DEVICE_BAND, systolic, diastolic, lastUpdated, 0)
                repository.insertBP(fitnessBP)
            } catch (e: Exception) {
                Log.e(TAG, "nfe", e)
            }
        }
    }

    fun insertSedentary() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val sedentary = Sedentary(0, featureStorage.sedentary, System.currentTimeMillis(), 0L)
                repository.insertSedentary(sedentary)
            } catch (e: Exception) {
                Log.e(TAG, "error", e)
            }
        }
    }

    fun insertStateSteps(state: Int, steps: Int, lastUpdated: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val stateSteps = StateSteps(0, state, "{state=$state,steps=$steps}", lastUpdated, 0L)
                repository.insertStateSteps(stateSteps)
            } catch (e: Exception) {
                Log.e(TAG, "error", e)
            }
        }
    }

    /**
     * Method to stop the active challenge session from notification
     */
    override fun onStopSession() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val summary = repository.getFanSocialRepository().stopFitnessActivity(
                        getApplication(), "", System.currentTimeMillis(), this)
                Log.d(TAG, "stopFitnessActivity $summary")
            } catch (e: Exception) {
                Log.e(TAG, "error in onStopSession", e)
            }
        }
    }

    companion object {
        private const val TAG = "FanFitCommon"
    }

    override fun getGoogleFitService(): GoogleFitService {
        return service
    }

    override fun onDeviceDisconnect() {
        stopProgress.postValue(true)
    }

    override fun getIntervalMinutes(): Int {
        return 2
    }

    override fun getType(): Int {
        return GOOGLE_FIT
    }

    override fun onPreExecute(taskId: Int) {
        if (taskId == HEART_RATE_TASK) startProgress.postValue(true)
    }

    override fun onPostExecute(taskId: Int) {
        if (taskId == HEART_RATE_TASK) {
            stopProgress.postValue(true)
            modeLive.value?.let { mode ->
                if (mode.toInt() == HeartRate.DEVICE_BAND) {
                    sdk.ble.requestSync()
                }
            }
        }
    }

    override fun onSuccessTask(taskId: Int) {
        //Log.d(TAG, "Fetch Activities success")
    }

    override fun onFailureTask(taskId: Int) {

    }

    override fun updateHeartRate(hr: Int, type: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 0, DEVICE_BAND, heartRate, System.currentTimeMillis(), 0
                if (type == GOOGLE_FIT) {
                    val fitnessHR = FitnessHR(0, GOOGLE_FIT, hr, System.currentTimeMillis(), 0L)
                    repository.insertHR(fitnessHR)
                    //Log.d(TAG, "heart rate $hr")
                } else if (type == HeartRate.CAMERA) {
                    val fitnessHR = FitnessHR(0, PHONE, hr, System.currentTimeMillis(), 0L)
                    repository.insertHR(fitnessHR)
                    stopProgress.postValue(true)
                    //Log.d(TAG, "camera heart rate $hr")
                }
            } catch (e: Exception) {
                Log.e(TAG, "error in updateHR", e)
            }
        }
    }

    override fun updateActivity(step: Int, calorie: Float, distance: Float, type: Int) {
        viewModelScope.launch {
            try {
                //Log.d(TAG, "step $step calories $calorie distance $distance")
                userLive.value?.let { user ->
                    val lastUpdated = System.currentTimeMillis()
                    val scd = FitnessSCD(0, GOOGLE_FIT, step,
                            getSDKCalories(step, user), //calorie.roundToInt().toFloat(),
                            getSDKDistance(step, user),
                            DISTANCE_UNIT_KM, step, lastUpdated, 0L)
                    repository.insertSCDForType(scd, GOOGLE_FIT)
                }
            } catch (e: Exception) {
                Log.e(TAG, "error in updateActivity", e)
            }
        }
    }

    override fun updatePhoneSteps(stepsCount: Int, endTs: Long) {
        if (modeLive.value?.toInt() ?: 0L != HeartRate.CAMERA) return
        userLive.value?.let { user ->
            viewModelScope.launch(Dispatchers.IO) {
                // interval passed store steps count in DB
                val scd = FitnessSCD(0, PHONE, stepsCount,
                        getSDKCalories(stepsCount, user),
                        getSDKDistance(stepsCount, user),
                        DISTANCE_UNIT_KM, 0, endTs, 0L)
                //Log.d(TAG, "Phone SCD ${scd.steps} ${scd.calories} ${scd.distance} ")
                repository.insertSCDForType(scd, PHONE)
            }
        }
    }

    override fun setReconnectFlow(flow: Flow<ConnectionResult>) {
        viewModelScope.launch {
            flow.collect { result ->
                when (result) {
                    is ConnectionResult.BloodPressureHistory -> {
                        insertBP(result.systolic, result.diastolic)
                    }
                    ConnectionResult.Connected.Initialized -> {
                        mDeviceReady.postValue(OneTimeEvent(false))
                    }
                    ConnectionResult.Connected.Synchronized -> {
                        mDeviceReady.postValue(OneTimeEvent(true))
                    }
                    ConnectionResult.Connected.SynchronizedHrBp -> {
                    }
                    ConnectionResult.Connecting -> {
                        mDeviceReady.postValue(OneTimeEvent(false))
                    }
                    ConnectionResult.Disconnected -> {
                        mDeviceReady.postValue(OneTimeEvent(false))
                    }
                    is ConnectionResult.FirmwareVersion -> {

                    }
                    is ConnectionResult.HeartRateHistory -> insertHR(result.heartRate, result.timestamp)
                    is ConnectionResult.StateSteps -> insertSCD(result.steps)
                }
            }
        }
    }

    private fun getSDKDistance(totalStepCount: Int, user: User) =
            sdk.sportModule.getDistanceWithHeightFloat(
                    totalStepCount,
                    getHeightForUser(user),
                    MSDefinition.UNIT_KILOMETERS)

    private fun getSDKCalories(totalStepCount: Int, user: User) =
            sdk.sportModule.getCalorieWithHeightAndWeight(
                    totalStepCount,
                    getHeightForUser(user),
                    getWeightForUser(user)
            ).toFloatOrNull() ?: 0f

    private fun getHeightForUser(user: User): Int {
        var height = user.height?.toIntOrNull() ?: MSDefinition.HEIGHT_DEFAULT_VALUE
        if (FEET.equals(user.heightMeasure, ignoreCase = true)) {
            height = (height * 30.48).toInt()
        }
        return height
    }

    private fun getWeightForUser(user: User): Int {
        var weight = user.weight?.toIntOrNull() ?: MSDefinition.WEIGHT_DEFAULT_VALUE
        if (LB.equals(user.weightMeasure, ignoreCase = true)) {
            weight = (weight / 2.205).toInt()
        }
        return weight
    }

    fun setSyncComplete() {
        mDeviceReady.value = OneTimeEvent(true)
    }

    fun logEvents(itemName: String) {
        AnalyticsService.logClickEvents(itemName, "FanFitFragment")
    }
}

@Suppress("UNCHECKED_CAST")
class FanFitViewModelFactory(private val common: FanFitCommon, private val homeViewModel: HomeViewModel) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val repository = common.repository
        if (modelClass.isAssignableFrom(FitnessViewModel::class.java)) {
            return FitnessViewModel(common, homeViewModel, repository) as T
        }
        if (modelClass.isAssignableFrom(SCDViewModel::class.java)) {
            return SCDViewModel(common, repository) as T
        }
        if (modelClass.isAssignableFrom(HrBpViewModel::class.java)) {
            return HrBpViewModel(common, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}


@Suppress("UNCHECKED_CAST")
class FanFitCommonVMFactory(private val application: Application, private val homeViewModel: HomeViewModel) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FanFitCommon::class.java)) {
            return FanFitCommon(application, homeViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
