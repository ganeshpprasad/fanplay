package com.fanplayiot.core.foreground.service

import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.util.Log
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class HRService : FitnessService() {
    companion object {
        private const val TAG = "HRService"
        private const val EXTRA_TYPE = "EXTRA_TYPE"
        private const val EXTRA_SESSION_ID = "EXTRA_SESSION_ID"

        @JvmStatic
        fun bindService(context: Context, connection: ServiceConnection, type: Int): Boolean {
            val startIntent = Intent(context.applicationContext, HRService::class.java).apply {
                putExtra(EXTRA_TYPE, type)
            }
            return context.applicationContext.bindService(startIntent, connection, Context.BIND_AUTO_CREATE)
        }

        @JvmStatic
        fun unBindService(context: Context, connection: ServiceConnection) {
            try {
                context.applicationContext.unbindService(connection)
            } catch (e: IllegalArgumentException) {
                // do nothing
                Log.d(TAG, "error in unBindService")
            }
        }
    }

    fun stopHRService() {
        stopForeground(true)
        cancelHr()
    }

    override fun getLocalBinder(): Binder = LocalBinder()

    /**
     * Class used for the client Binder.
     */
    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): HRService = this@HRService
    }

    fun initHeartRateFanEngageDeviceBand(listener: FanEngageListener) {
        caller = Caller.FanEngage
        fanEngageListener = listener
        initHR()
        Log.d(TAG, "initHeartRateFanEngageDeviceBand")
    }

    fun initHeartRateFanEngageGoogleFit(listener: FanEngageListener) {
        caller = Caller.FanEngage
        fanEngageListener = listener
        googleFitService = listener.getGoogleFitService()
        initHR()
        Log.d(TAG, "initHeartRateFanEngageGoogleFit")
    }

    fun initHeartRateFanFitDeviceBand(listener: FanFitListener) {
        caller = Caller.FanFit
        fanFitListener = listener
        initHR()
        Log.d(TAG, "initHeartRateFanFit Device band")
    }

    fun initHeartRateFanFitGoogleFit(listener: FanFitListener) {
        caller = Caller.FanFit
        fanFitListener = listener
        googleFitService = listener.getGoogleFitService()
        initHR()
        Log.d(TAG, "initHeartRateFanFitGoogleFit")
    }

    fun initSessionFanFitPhone() {
        caller = Caller.FanFit
        initHR()
        Log.d(TAG, "initHeartRateFanFitGoogleFit")
    }
}