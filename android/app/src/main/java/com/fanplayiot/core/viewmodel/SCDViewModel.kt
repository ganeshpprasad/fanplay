package com.fanplayiot.core.ui.home.fanfit

import androidx.lifecycle.ViewModel
import com.fanplayiot.core.bluetooth.SDKManager
import com.fanplayiot.core.db.local.repository.FitnessRepository
import com.fanplayiot.core.remote.firebase.analytics.AnalyticsService
import com.fanplayiot.core.viewmodel.FanFitCommon

class SCDViewModel(val common: FanFitCommon, val repository: FitnessRepository) : ViewModel() {

    val sdk = common.sdk
    val scdLive = repository.fitnessSCDLive
    val hrLive = repository.fitnessHRLive
    val bpLive = repository.fitnessBPLive
    val scoreLive = repository.fanFitScoreLive
    val modeLive = repository.modeLive
    var bpTriggered = false
    var hrReadingRunning = false

    fun startBpReading() {
        if (bpTriggered) return;
        bpTriggered = true;
        sdk.userModule.setBloodPressureTest(bpTriggered)
    }

    fun stopBpReading() {
        bpTriggered = false;
        sdk.userModule.setBloodPressureTest(bpTriggered)
    }

    fun startHrReading() {
        modeLive.value?.toInt()?.let {
            common.startHeartRate.value = it
            hrReadingRunning = true
        }
    }

    fun stopHrReading() {
        modeLive.value?.toInt()?.let {
            common.stopHeartRate.value = it
            hrReadingRunning = false
        }
    }

    fun logScreenView(screenName: String) {
        AnalyticsService.logScreenView(screenName, "FanFitViewFragment")
    }

    fun logEvents(itemName: String) {
        AnalyticsService.logClickEvents(itemName, "FanFitViewFragment")
    }
}