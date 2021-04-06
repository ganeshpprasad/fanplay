package com.demoproject

import android.bluetooth.BluetoothProfile
import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactMethod
import com.fanplayiot.core.bluetooth.SDKManager
import com.fanplayiot.core.db.local.repository.DevicesRepository
import com.fanplayiot.core.utils.AbstractRNModule

class DevicesModule(reactContext: ReactApplicationContext) : AbstractRNModule(reactContext) {
    private val repository: DevicesRepository = DevicesRepository(reactContext.applicationContext)
    private val manager: SDKManager = SDKManager.instance(reactContext.applicationContext)

    override fun getName(): String {
        return "DevicesModule"
    }

    @ReactMethod
    fun init() {

    }

    @ReactMethod
    fun scan() {
        manager.ble.stopReConnect()
        manager.device_address = null
        manager.ble.scan(15)
    }

    @ReactMethod
    fun stopScan() {
        manager.ble.stopScan()
    }

    @ReactMethod
    fun startHeartRate() {

    }

    @ReactMethod
    fun startSteps() {

    }

    @ReactMethod
    fun connect(device: String, callback: Callback) {
        manager.ble.stopScan()
        manager.ble.stopReConnect()
        manager.ble.connect(device)
        callback.invoke(true)
    }

    @ReactMethod
    fun disconnect(device: String, callback: Callback) {

    }

    @ReactMethod
    fun isConnected(callback: Callback) {
        callback.invoke(manager.ble.connectionState == BluetoothProfile.STATE_CONNECTED)
    }

    @ReactMethod
    fun insertBand(macAddress: String) {

    }
}