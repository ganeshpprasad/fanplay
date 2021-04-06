package com.fanplayiot.core.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.util.Log
import android.util.Pair
import androidx.arch.core.util.Function
import androidx.lifecycle.*
import com.clj.fastble.data.BleDevice
import com.fanplayiot.core.bluetooth.FastBleManager
import com.fanplayiot.core.bluetooth.SDKManager
import com.fanplayiot.core.bluetooth.WaveNotifyListener
import com.fanplayiot.core.db.local.entity.*
import com.fanplayiot.core.db.local.repository.FanEngageRepository
import com.fanplayiot.core.foreground.service.FanEngageListener
import com.fanplayiot.core.googlefit.GoogleFitService
import com.fanplayiot.core.googlefit.ServiceCallback
import com.fanplayiot.core.remote.firebase.analytics.AnalyticsService
import com.fanplayiot.core.remote.pojo.FanEmote
import com.fanplayiot.core.utils.CombinedLiveData
import com.fanplayiot.core.utils.Constant
import com.mcube.ms.sdk.definitions.MSDefinition

class FanEngageViewModel(application: Application, @JvmField val homeViewModel: HomeViewModel) : AndroidViewModel(application), WaveNotifyListener, ServiceCallback, FanEngageListener {
    private val repository: FanEngageRepository

    @JvmField
    var bandLive: LiveData<Device?>

    @JvmField
    var emoteLive: LiveData<Device?>

    @JvmField
    var fanDataLive: LiveData<FanData?>

    @JvmField
    var heartRateLive: LiveData<HeartRate?>

    @JvmField
    var waveDataLive: LiveData<WaveData?>

    @JvmField
    var whistleDataLive: LiveData<WhistleData?>

    @JvmField
    var fansEmoteLive: LiveData<FanEmote?>

    @JvmField
    var fanPicture: LiveData<String?>

    @JvmField
    var modeLive: LiveData<Long?>
    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private val fastBleManager: FastBleManager

    @JvmField
    var fitService: GoogleFitService
    private val bleDeviceLive = MutableLiveData<BleDevice?>()

    @JvmField
    var waveCountLive = MutableLiveData<Int>()

    @JvmField
    var whistleLive = MutableLiveData<Boolean>()

    @JvmField
    var isFitHrNoValue = MutableLiveData(false)

    @JvmField
    var stopProgress = MutableLiveData(false)

    @JvmField
    var startProgress = MutableLiveData(false)

    @JvmField
    var isHRTriggered = false

    @JvmField
    var isSync = false

    //public boolean isGFitRunning = false;
    @JvmField
    var hrMode: LiveData<Int?> = MutableLiveData(null)

    @JvmField
    var currentHrMode = 0

    @JvmField
    var isEmoteWave = MutableLiveData(false)
    private val analyticsService = AnalyticsService
    private val sdk = SDKManager.instance(getApplication())

    init {
        repository = FanEngageRepository(application)
        bandLive = repository.band
        emoteLive = repository.emote
        fanDataLive = repository.fanDataLiveData
        heartRateLive = repository.heartRateLiveData
        waveDataLive = repository.waveDataLiveData
        whistleDataLive = repository.whistleDataLiveData
        fansEmoteLive = repository.fansEmoteLive
        fanPicture = repository.fanPicture
        modeLive = repository.modeLive
        fastBleManager = FastBleManager(getApplication())
        fitService = GoogleFitService(this)
        val trigger = CombinedLiveData(bandLive, repository.modeLive)
        hrMode = Transformations.map(trigger, Function<Pair<Device, Long>, Int?> { input ->
            if (input.first != null && input.second != null) {
                if (input.second!! > 0
                        && input.second!!.toInt() == HeartRate.DEVICE_BAND) input.second!!.toInt() else null
            } else null
        })
    }

    override fun onCleared() {
        super.onCleared()
        if (bleDeviceLive.value != null) {
            fastBleManager.removeNotify(bleDeviceLive.value)
            bleDeviceLive.value = null
        }
        //fastBleManager.disconnectAll();
        currentHrMode = 0
        //bluetoothAdapter.disable();
    }

    fun logEvents(clickItem: String?) {
        analyticsService.logClickEvents(clickItem!!, "MainFanEngagementFragment")
    }

    fun logUserRefreshed() {
        analyticsService.logCustomEvents()
    }

    fun getFanEmote() {
        repository.getFanEmote()
    }

    fun updateTapCount(count: Int, playerId: Int) {
        //Log.d(TAG, " player id " + playerId);
        repository.insertFanData(count, playerId)
    }

    fun updateHRPref(@HeartRateType type: Int) {
        Log.d(TAG, "HR reading preference $type")
        repository.updateHRPref(type)
    }

    fun updateWaveCount(count: Int, type: Int) {
        var waveData = waveDataLive.value
        if (waveData == null) waveData = WaveData()
        val lastUpdated = System.currentTimeMillis()
        waveData.waveCount = count
        waveData.waveType = type
        waveData.lastUpdated = lastUpdated
        repository.insertWaveData(waveData)
    }

    // Call this instead of decreaseWhistleCount
    fun incrementWhistle() {
        var whistleData = whistleDataLive.value
        if (whistleData == null) whistleData = WhistleData()
        val lastUpdated = System.currentTimeMillis()
        var count = whistleData.whistleRedeemed
        whistleData.whistleRedeemed = ++count
        whistleData.lastUpdated = lastUpdated
        repository.insertWhistleData(whistleData)
    }

    val isEmoteConnected: LiveData<FastBleManager.State>
        get() = fastBleManager.stateLive

    fun sync(): Boolean {
        if (bandLive.value == null) return false
        if (!isBluetoothEnabled) return false
        val fanData = fanDataLive.value
        if (fanData != null && fanData.flag > 0 && fanData.flag != HeartRate.DEVICE_BAND.toLong()) {
            Log.d(TAG, "Current HR mode " + fanData.flag)
            Log.d(TAG, "Not syncing")
            return false
        }
        val address = bandLive.value!!.address
        val connectionState = sdk.ble.connectionState
        if (connectionState != BluetoothProfile.STATE_CONNECTED && connectionState != BluetoothProfile.STATE_CONNECTING) {
            sdk.ble.startReConnect(address)
            return true
        }
        return false
    }

    val isBluetoothEnabled: Boolean
        get() = bluetoothAdapter.isEnabled
    val isConnected: Boolean
        get() = sdk.ble.connectionState == BluetoothProfile.STATE_CONNECTED
    val isDisconnected: Boolean
        get() = sdk.ble.connectionState == BluetoothProfile.STATE_DISCONNECTED

    fun disconnect() {
        //isHR = false;
        sdk.userModule.setHeartRateTest(MSDefinition.HEART_RATE_TEST_STOP)
        sdk.ble.stopReConnect()
        currentHrMode = 0
        isSync = false
    }

    fun deleteDevice() {}
    fun connectEmote() {
        if (emoteLive.value == null) return
        fastBleManager.connectForMAC(emoteLive.value!!.address, this)
    }

    @SuppressLint("MissingPermission")
    fun onDeviceConnected() {
        Log.d(TAG, "device connected ")
        sdk.firmware.readBatteryLevel()
        //sdk.getBLE().startConnectionFlow();
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (bandLive.value == null) return
        val address = bandLive.value!!.address
        val device: BluetoothDevice = adapter?.getRemoteDevice(address) ?: return
        if (device.createBond()) {
            Log.d(TAG, "device bond created ")
        }
    }

    fun onSyncSuccess() {
        val bondState = sdk.ble.bondState
        Log.d(TAG, "device sync success bond state : $bondState")
        /*if (bondState) {
            sdk.getBLE().startConnectionFlow();
        }*/
        //sdk.getFirmware().readBatteryLevel();
        sdk.userModule.setTime()
        Log.d(TAG, """
     Steps ${sdk.steps}
     Calories ${sdk.calorie}
     Distance ${sdk.distance}
     """.trimIndent())
    }

    override fun updateHeartRate(hr: Int, @HeartRateType type: Int) {
        val heartRate = HeartRate()
        val lastUpdated = System.currentTimeMillis()
        heartRate.heartRate = hr
        heartRate.type = type
        heartRate.lastUpdated = lastUpdated
        repository.insertHeartRate(heartRate)
        Log.d(TAG, "heart rate $hr")
    }

    override fun onConnectFail() {
        isEmoteWave.postValue(false)
        Log.d(TAG, "Emote connection failed")
    }

    override fun onConnectSuccess(bleDevice: BleDevice) {
        bleDeviceLive.postValue(bleDevice)
        Log.d(TAG, "Emote connection success")
    }

    override fun onUpdateWave(count: Int) {
        waveCountLive.postValue(count)
    }

    override fun invokeWhistle() {
        whistleLive.postValue(true)
    }

    override fun onDisconnect() {
        isEmoteWave.postValue(false)
    }

    override fun getGoogleFitService(): GoogleFitService {
        return fitService
    }

    override fun onDeviceDisconnect() {
        stopProgress.postValue(true)
    }

    override fun getIntervalMinutes(): Int {
        return 2
    }

    override fun getType(): Int {
        return HeartRate.GOOGLE_FIT
    }

    override fun onPreExecute(taskId: Int) {
        startProgress.postValue(true)
    }

    override fun onPostExecute(taskId: Int) {
        stopProgress.postValue(true)
    }

    override fun onSuccessTask(taskId: Int) {
        isFitHrNoValue.value = false
    }

    override fun onFailureTask(taskId: Int) {
        isFitHrNoValue.postValue(true)
    }

    override fun updateActivity(step: Int, calorie: Float, distance: Float, type: Int) {
        // do nothing
    }

    companion object {
        private const val TAG = "FanEngageViewModel"

        @JvmField
        var filters = arrayOf(
                Constant.FOUND_DEVICE, Constant.DEVICE_CONNECT_STATE, Constant.SYNC_STATE)
    }

}

@Suppress("UNCHECKED_CAST")
class FanEngageVMFactory(private val application: Application, private val homeViewModel: HomeViewModel) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FanEngageViewModel::class.java)) {
            return FanEngageViewModel(application, homeViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}