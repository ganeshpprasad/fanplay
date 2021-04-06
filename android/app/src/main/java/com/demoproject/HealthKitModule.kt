package com.demoproject

import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactMethod
import com.fanplayiot.core.utils.AbstractRNModule

class HealthKitModule(reactContext: ReactApplicationContext) : AbstractRNModule(reactContext) {
    override fun getName(): String {
        return "HealthKitModule"
    }

    @ReactMethod
    fun init() {

    }

    @ReactMethod
    fun isHealthAvailable(callback: Callback) {
        callback.invoke(true)
    }

    @ReactMethod
    fun requestAuthorization(callback: Callback) {

        callback.invoke(true, "")
    }

    @ReactMethod
    fun getHeartBeat() {

    }
}