package com.fanplayiot.core.utils

import android.util.Log
import com.facebook.react.ReactInstanceManager
import com.facebook.react.bridge.Arguments
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.fanplayiot.core.db.local.entity.json.SessionSummary
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val TAG = "MainDrawer"

fun ReactInstanceManager.emitEvent(started: Boolean, id: Long) {
    val params = Arguments.createMap()
    // pass data instead of jsonData if data is a String
    params.putBoolean("sessionStarted", started)
    params.putDouble("sessionId", id.toDouble())
    this.currentReactContext?.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)?.
            emit("onSessionState", params) ?: run {
        Log.d(TAG, "error in emit onSessionState")
    }
    Log.d(TAG, "session info $started $id")
}

fun ReactInstanceManager.emitSummary(summary: SessionSummary?) {
    val params = Arguments.createMap()
    // pass data instead of jsonData if data is a String
    params.putString("summary", Json.encodeToString(summary))
    this.currentReactContext?.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)?.
            emit("onSessionSummary", params)
    //Log.d(TAG, "session info $started $id")
}