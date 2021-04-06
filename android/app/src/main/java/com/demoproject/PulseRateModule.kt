package com.demoproject

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactMethod
import com.fanplayiot.core.utils.AbstractRNModule

class PulseRateModule (reactContext: ReactApplicationContext) : AbstractRNModule(reactContext) {
    override fun getName(): String {
        return "PulseRateModule"
    }

    @ReactMethod
    fun init() {

    }

    @ReactMethod
    fun getPulseRate() {

    }
}