package com.fanplayiot.core.foreground.service

import com.fanplayiot.core.bluetooth.ConnectionResult
import com.fanplayiot.core.googlefit.GoogleFitService
import com.fanplayiot.core.googlefit.ServiceCallback
import kotlinx.coroutines.flow.Flow

interface FanFitListener : ServiceCallback {
    fun updatePhoneSteps(stepsCount: Int, endTs: Long)
    fun insertHR(heartRate: Int, lastUpdated: Long = System.currentTimeMillis())
    fun getGoogleFitService() : GoogleFitService
    fun onDeviceDisconnect()
    fun onStopSession()
    fun setReconnectFlow(flow: Flow<ConnectionResult>)
}


interface FanEngageListener : ServiceCallback {
    fun getGoogleFitService() : GoogleFitService
    fun onDeviceDisconnect()
}