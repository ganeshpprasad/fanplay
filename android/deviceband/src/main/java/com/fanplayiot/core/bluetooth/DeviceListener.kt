package com.fanplayiot.core.bluetooth

import android.bluetooth.BluetoothDevice
import com.clj.fastble.data.BleDevice
import com.mcube.ms.sdk.interfaces.MSCallbacks

interface SDKCallback {

    fun onStartSync()

    fun onSyncStarted()

    fun onSyncEnd()

    fun onConnectionStateChanged(state: Int)

    fun onHeartRateChanged(hr: Int)

}

interface SDKConnectionCallback {

    fun onConnectionStateChanged(state: Int?)

    fun onFirmwareVersionRead(version: String?, pair: Boolean, hrBp: Boolean, oxygen: Boolean)

    fun onStateAndStepsChanged(state: Int?, steps: Int?)

    fun onSyncHistories(address: String?, state: Int?, steps: Int?, start: Long?)

    fun onSyncCurrentState(address: String?, state: Int?, steps: Int?, start: Long?, far: Int?)

    fun onStartSync()

    fun onSyncEnd()

    fun onHrSyncHistories(address: String?, hr: Int?, time: Long?)

    fun onBpSyncHistories(address: String?, systolic: Int?, diastolic: Int?, time: Long?)

    fun onHrBpSyncEnd()
}