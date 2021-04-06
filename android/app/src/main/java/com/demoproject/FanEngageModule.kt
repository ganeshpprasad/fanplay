package com.demoproject

import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactMethod
import com.fanplayiot.core.db.local.repository.FanEngageRepository
import com.fanplayiot.core.utils.AbstractRNModule
import kotlinx.coroutines.launch

class FanEngageModule(reactContext: ReactApplicationContext) : AbstractRNModule(reactContext) {
    val fanEngageRepository = FanEngageRepository(context)
    override fun getName(): String {
        return "FanEngageModule"
    }

    @ReactMethod
    fun init() {

    }

    @ReactMethod
    fun validateSignIn(callback: Callback) {

    }

    @ReactMethod
    fun getFanEmote(teamId: Double, callback: Callback) {
        fanEngageRepository.getFanEmoteResponse { response ->
            response?.let { callback.invoke(it) }
        }

    }

    @ReactMethod
    fun getFanEngageData(callback: Callback) {
        callback.invoke("{ \"tapcounts\": 9, \"wavecounts\": 68, \"whistlesredeemed\": 0, \"whistlecounts\": 8,}")
    }

    @ReactMethod
    fun getFEDetailsByTeamId(teamId: Double, callback: Callback) {
        fanEngageRepository.getFEDetailsByTeamId {
            it?.let { callback.invoke(it) }
        }

 /*
 {
        "tapcounts": 9,
        "wavecounts": 68,
        "whistlesredeemed": 0,
        "whistlecounts": 8,
        "fescore": 6.9,
        "points": 826
      }
  */

    }

    @ReactMethod
    fun incrementWhistle(one: Double, two: Double, three: Double) {

    }

    @ReactMethod
    fun startFanEngageHeartRate(type: Double) {

    }

    @ReactMethod
    fun stopFanEngageHeartRate() {

    }

    @ReactMethod
    fun startSteps() {

    }

    @ReactMethod
    fun sync(type: Double, callback: Callback) {
        callback.invoke(true)
    }
}