package com.fanplayiot.core.foreground.service

import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Binder
import android.util.Log
import com.fanplayiot.core.api.PedometerService
import com.fanplayiot.core.api.StepCounterEventListener
import com.fanplayiot.core.bluetooth.ConnectionResult
import com.fanplayiot.core.bluetooth.reconnectAndSync
import com.fanplayiot.core.db.local.entity.DEVICE_BAND
import com.fanplayiot.core.db.local.entity.FitnessSCD
import com.fanplayiot.core.db.local.entity.GOOGLE_FIT
import com.fanplayiot.core.googlefit.ActivityTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.fixedRateTimer

@ExperimentalCoroutinesApi
class SCDService : FitnessService(), StepCounterEventListener.IStepCounterListenerConsumer {
    companion object {
        private const val TAG = "SCDService"
        const val EXTRA_TYPE = "EXTRA_TYPE"
        private const val FIT_TIMER_IN_FUTURE = 30 * 1000L // SCD from Google fit interval in millis
        private const val SCD_TIMER_IN_FUTURE = 60 * 1000L // SCD from Device band interval in millis
        private const val SCD_DELAY = 30 * 1000L // SCD from Device band delay in millis

        @JvmStatic
        fun bindService(context: Context, connection: ServiceConnection, type: Int): Boolean {
            val startIntent = Intent(context.applicationContext, SCDService::class.java)
            startIntent.putExtra(EXTRA_TYPE, type)
            return context.applicationContext.bindService(startIntent, connection, Context.BIND_AUTO_CREATE)
        }

        @JvmStatic
        fun unBindService(context: Context, connection: ServiceConnection) {
            try {
                context.applicationContext.unbindService(connection)
            } catch (e: IllegalArgumentException) {
                // do nothing
            }
        }
    }

    private lateinit var phoneStepListener: StepCounterEventListener
    private lateinit var timer: Timer
    private var startTs = 0L
    private var endTs = 0L
    private var stepsCount = 0
    private var sensorUpdateEnabled = false
    private var gfitSyncState = false

    override fun getLocalBinder(): Binder = LocalBinder()

    /**
     * Class used for the client Binder.
     */
    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): SCDService = this@SCDService
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::phoneStepListener.isInitialized) PedometerService.close(phoneStepListener)
        if (this::timer.isInitialized) timer.cancel()
        sensorUpdateEnabled = false
        gfitSyncState = false
    }

    fun stopService() {
        if (this::phoneStepListener.isInitialized) PedometerService.close(phoneStepListener)
        if (this::timer.isInitialized) timer.cancel()
        sensorUpdateEnabled = false
        gfitSyncState = false
    }

    fun initPhonePedometer(listener: FanFitListener) {
        caller = Caller.FanFit
        fanFitListener = listener
        if (this::phoneStepListener.isInitialized) PedometerService.close(phoneStepListener)
        if (!applicationContext.packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER)) {
            Log.d(TAG, "step counter not available")
            sensorUpdateEnabled = false
            return
        }
        phoneStepListener = StepCounterEventListener(this)
        PedometerService.init(application, phoneStepListener)
        sensorUpdateEnabled = true
        gfitSyncState = false
    }

    fun initGoogleFitSCD(listener: FanFitListener) {
        caller = Caller.FanFit
        fanFitListener = listener
        googleFitService = listener.getGoogleFitService()

        //do heavy work on a background thread
        try {
            when (deviceType) {
                GOOGLE_FIT -> {
                    checkGoogleFitOrStopSelf()
                    gfitSyncState = true
                    timerState = TimerState.Running
                    timer = fixedRateTimer(TAG, false, 0, FIT_TIMER_IN_FUTURE) {
                        if (deviceType != GOOGLE_FIT) cancel()
                        googleFitService.historyClient?.let { historyClient ->
                            val task = ActivityTask(historyClient, listener)
                            coroutineScope.launch(Dispatchers.IO) {

                                googleFitService.subscribeToTask(task)
                            }

                            Log.d(TAG, "activity gfit triggered")
                        }
                    }
                }
                else -> {
                    if (this::timer.isInitialized) timer.cancel()
                    if (this::phoneStepListener.isInitialized) PedometerService.close(phoneStepListener)
                    timerState = TimerState.Stopped
                    gfitSyncState = false
                }

            }
        } catch (e: Exception) {
            Log.e(TAG, "error ", e)
        } finally {
            timerState = TimerState.Stopped
        }
        stopSelf()
    }

    fun initDeviceBandSCD(address: String): Flow<ConnectionResult> {
        return sdk.reconnectAndSync(address)
    }

    override fun onSensorEvent(stepsDelta: Int) {
        if (startTs == 0L) {
            startTs = System.currentTimeMillis()
            endTs = 0L
            stepsCount = stepsDelta

        } else {
            endTs = System.currentTimeMillis()
            stepsCount += stepsDelta
            if (endTs - startTs >= FIT_TIMER_IN_FUTURE) {
                startTs = 0L
                if (isFanFitInitialized()) {
                    fanFitListener.updatePhoneSteps(stepsCount, endTs)
                }
            }
        }
    }

}