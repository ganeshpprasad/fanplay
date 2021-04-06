package com.fanplayiot.core.foreground.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.demoproject.R
import com.fanplayiot.core.bluetooth.*
import com.fanplayiot.core.db.local.entity.*
import com.fanplayiot.core.db.local.entity.json.SessionSummary
import com.fanplayiot.core.db.local.repository.FitnessRepository
import com.fanplayiot.core.googlefit.GoogleFitService
import com.fanplayiot.core.googlefit.HEART_RATE_TASK
import com.fanplayiot.core.googlefit.HeartRateTask
import com.fanplayiot.core.utils.NotificationUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import java.util.*
import kotlin.concurrent.fixedRateTimer
import kotlin.math.roundToInt

@ExperimentalCoroutinesApi
abstract class FitnessService : Service() {
    companion object {
        private const val TAG = "FitnessService"
        const val EXTRA_TYPE = "EXTRA_TYPE"
        const val EXTRA_SESSION_ID = "EXTRA_SESSION_ID"
        const val EXTRA_CALLER = "EXTRA_CALLER"
        //private const val FIT_TIMER_IN_FUTURE = 30 * 1000L // SCD from Google fit interval in millis
        private const val HR_MS_INFUTURE = 60000L //120000L // Heart rate from band repeat interval in millis
        //private const val HR_TIMER_INFUTURE = 40000L // Heart rate from band count down timer UI in ms
        //private const val HR_SYNC_INFUTURE = 8000L // Device sync timeout in millis

    }

    enum class TimerState {
        Stopped, Running
    }

    enum class Caller {
        FanEngage, FanFit
    }

    protected var timerState = TimerState.Stopped
    private var hrTimerState = TimerState.Stopped
    protected var caller: Caller? = Caller.FanFit
    protected lateinit var googleFitService: GoogleFitService
    protected lateinit var fanFitListener: FanFitListener
    protected lateinit var fanEngageListener: FanEngageListener
    private lateinit var hrTimer: Timer
    private val job = SupervisorJob()
    protected val coroutineScope = CoroutineScope(Dispatchers.IO + job)
    protected var deviceType: Int = -1
    var sessionId: Long = -1L
    protected lateinit var sdk: SDKManager
    protected lateinit var repository: FitnessRepository
    private val mProgressLive = MutableLiveData<SessionSummary>(null)
    val progressLive: LiveData<SessionSummary>
        get() = mProgressLive

    abstract fun getLocalBinder() : Binder

    /**
     * start command which returns START_NOT_STICKY.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelHr()
        job.cancel(CancellationException())
    }

    private fun isFanEngageInitialized() = ::fanEngageListener.isInitialized

    protected fun isFanFitInitialized() = ::fanFitListener.isInitialized

    private fun isCallerInitialized() {
        if (!isFanEngageInitialized() && caller == Caller.FanEngage) {
            stopSelf()
            return
        }
        if (!isFanFitInitialized() && caller == Caller.FanFit) {
            stopSelf()
            return
        }
    }

    fun cancelHr() {
        coroutineScope.launch {
            coroutineContext.cancelChildren(CancellationException())
            Log.d(TAG, "cancelled flow")
        }
        //sdk.userModule.setHeartRateTest(MSDefinition.HEART_RATE_TEST_STOP)
        if (this::hrTimer.isInitialized) hrTimer.cancel()
        hrTimerState = TimerState.Stopped
        caller = null
        deviceType = -1
        stopSelf()
    }


    private fun isGoogleFitAllowed() = googleFitService.checkPermissions(applicationContext)

    protected fun checkGoogleFitOrStopSelf() {
        if (!this::googleFitService.isInitialized) {
            stopSelf()
            return
        }
        if (!isGoogleFitAllowed()) {
            stopSelf()
            return
        }
        if (googleFitService.historyClient == null) {
            stopSelf()
            return
        }
    }

    fun getType() = deviceType

    protected fun initHR() {
        try {
            when (deviceType) {
                DEVICE_BAND -> {
                    isCallerInitialized()
                    if (this::hrTimer.isInitialized) hrTimer.cancel()

                    hrTimerState = TimerState.Running
                    coroutineScope.launch {
                        try {
                            if (!isActive) {
                                return@launch
                            }
                            sdk.startHeartRateFlow().collect { result ->
                                if (!isActive) {
                                    return@collect
                                }
                                if (deviceType != DEVICE_BAND) {
                                    return@collect
                                }

                                startForeground(NotificationUtils.NOTIFICATION_SERVICE_READ_HR_ID,
                                        NotificationUtils.startForegroundNotify(
                                                applicationContext,
                                                getString(R.string.reading_hr),
                                                NotificationUtils.NOTIFICATION_SERVICE_READ_HR_ID,
                                                getBundle(sessionId, caller?.name),
                                                getContentText()
                                        ))

                                when (result) {
                                    is HRResult.HeartRateValue -> {
                                        Log.d(TAG, "hr collected for caller $caller")
                                        when (caller) {
                                            Caller.FanEngage -> {
                                                fanEngageListener.updateHeartRate(result.heartRate, HeartRate.DEVICE_BAND)
                                                fanEngageListener.onPostExecute(HEART_RATE_TASK)
                                            }
                                            Caller.FanFit -> {
                                                fanFitListener.insertHR(result.heartRate, System.currentTimeMillis())
                                                fanFitListener.onPostExecute(HEART_RATE_TASK)
                                                storeSCD()
                                                //sdk.ble.requestSync()
                                            }
                                        }
                                    }
                                    HRResult.StartedReading -> {
                                        preExecute()
                                    }
                                    HRResult.Disconnected -> {

                                        when (caller) {
                                            Caller.FanEngage -> {
                                                fanEngageListener.onDeviceDisconnect()
                                            }
                                            Caller.FanFit -> {
                                                fanFitListener.onDeviceDisconnect()
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (ce: CancellationException) {
                            // do nothing
                        } catch (e: Exception) {
                            Log.e(TAG, "error in startHeartRateFlow", e)
                        }
                    }
                }
                GOOGLE_FIT -> {
                    checkGoogleFitOrStopSelf()
                    if (this::hrTimer.isInitialized) hrTimer.cancel()
                    hrTimerState = TimerState.Running
                    hrTimer = fixedRateTimer(TAG, false, 0, HR_MS_INFUTURE) {
                        if (deviceType != GOOGLE_FIT) {
                            hrTimerState = TimerState.Stopped
                            cancel()
                            return@fixedRateTimer
                        }

                        googleFitService.historyClient?.let { historyClient ->
                            val hrTask = HeartRateTask(historyClient,
                                    if (caller == Caller.FanEngage) fanEngageListener
                                    else fanFitListener
                            )
                            coroutineScope.launch(Dispatchers.IO) {
                                googleFitService.subscribeToTask(hrTask)
                                startForeground(NotificationUtils.NOTIFICATION_SERVICE_READ_HR_ID,
                                        NotificationUtils.startForegroundNotify(
                                                applicationContext,
                                                getString(R.string.reading_hr_gfit),
                                                NotificationUtils.NOTIFICATION_SERVICE_READ_HR_ID,
                                                getBundle(sessionId, caller?.name),
                                                getContentText()
                                        ))

                            }

                            //Log.d(TAG, "hr triggered gfit")
                        } ?: run {
                            stopSelf()
                        }
                    }
                }
                PHONE -> {
                    if (this::hrTimer.isInitialized) hrTimer.cancel()
                    hrTimerState = TimerState.Running
                    hrTimer = fixedRateTimer(TAG, false, 0, HR_MS_INFUTURE) {
                        if (deviceType != PHONE) {
                            hrTimerState = TimerState.Stopped
                            cancel()
                            return@fixedRateTimer
                        }
                        coroutineScope.launch(Dispatchers.IO) {
                            startForeground(NotificationUtils.NOTIFICATION_SERVICE_READ_HR_ID,
                                    NotificationUtils.startForegroundNotify(
                                            applicationContext,
                                            getString(R.string.reading_phone),
                                            NotificationUtils.NOTIFICATION_SERVICE_READ_HR_ID,
                                            getBundle(sessionId, caller?.name),
                                            getContentText()
                                    ))
                        }
                        Log.d(TAG, "Session running for Phone")
                    }
                }
                else -> {

                }

            }
        } catch (e: Exception) {
            Log.e(TAG, "error ", e)
        } finally {
            hrTimerState = TimerState.Stopped
        }
        stopSelf()
    }

    fun getDeviceBandSCD(listener: FanFitListener) {
        caller = Caller.FanFit
        fanFitListener = listener

        //do heavy work on a background thread
        try {
            when (deviceType) {
                DEVICE_BAND -> {
                    coroutineScope.launch(Dispatchers.IO) {
                        if (!isActive) {
                            return@launch
                        }
                        storeSCD()

                    }
                    Log.d(TAG, "activity SCD device band triggered")

                }

            }
        } catch (e: Exception) {
            Log.e(TAG, "error ", e)
        }
    }

    private suspend fun storeSCD() {
        try {
            val strCalorie = sdk.calorie?.trim() ?: ""
            val strDistance = sdk.distance?.replace("km", "")?.trim() ?: ""
            if (strCalorie.isNotEmpty() && strDistance.isNotEmpty()) {
                val scd = FitnessSCD(sdk.steps, strCalorie.toFloat(), strDistance.toFloat())
                repository.insertSCD(scd, DEVICE_BAND)
            }
        } catch (e: Exception) {
            Log.e(TAG, "nfe", e)
        }
    }

    protected fun preExecute() {
        if (caller == Caller.FanEngage) fanEngageListener.onPreExecute(HEART_RATE_TASK)
        else fanFitListener.onPreExecute(HEART_RATE_TASK)
    }

    override fun onBind(intent: Intent?): IBinder? {
        deviceType = intent?.getIntExtra(EXTRA_TYPE, -1) ?: -1
        sessionId = intent?.getLongExtra(EXTRA_SESSION_ID, -1L) ?: -1L
        repository = FitnessRepository(applicationContext)
        sdk = SDKManager.instance(applicationContext)
        return getLocalBinder()
    }

    private fun getBundle(paramSessionId: Long?, paramCaller: String?) : Bundle {
        return Bundle().also {
            it.putInt(EXTRA_TYPE, deviceType)
            it.putLong(EXTRA_SESSION_ID, paramSessionId ?: -1)
            it.putString(EXTRA_CALLER, paramCaller)
        }
    }

    private suspend fun getContentText(): String? {
        // progressLive
        repository.getFanSocialRepository().
        getProgress(System.currentTimeMillis(), "Test")?.let { summary ->
            Log.d(TAG, "summary $summary")
            mProgressLive.postValue(summary)
            return getString(R.string.session_progress, summary.durationInMins,
                    ((summary.distance * 10.00).roundToInt() / 10.00).toString().plus(" Km"),
                    summary.calories.toString().plus(" K Cal"),
                    summary.heartRate.toString().plus(" bpm"), summary.totalHr,
                    summary.steps.toString()
            )
            //" Avg Hr ${summary.heartRate}\n Steps ${summary.steps}\n Duration ${summary.durationInMins}"
        }
        if (sessionId != -1L) {
            return getString(R.string.session_progress, "0", "0",
                    "0",
                    "0", "0", "0")
        } else {
            return null
        }
    }

    fun alreadyRunning(type: Int, caller: Caller) = (this.deviceType == type
            && this.caller == caller
            && (hrTimerState == TimerState.Running || job.isActive))

}