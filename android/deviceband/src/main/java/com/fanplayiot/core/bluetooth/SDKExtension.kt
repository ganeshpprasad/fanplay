package com.fanplayiot.core.bluetooth

import android.bluetooth.BluetoothProfile
import android.util.Log
import com.mcube.ms.sdk.definitions.MSDefinition
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlin.concurrent.fixedRateTimer

private const val TAG = "FitnessService"
private const val CONN_TAG = "ConnectionService"
private const val HR_MS_INFUTURE = 60000L //120000L // Heart rate from band repeat interval in millis
private const val BAND_CHECK_MS_INFUTURE = 15 * 60000L // check band connection status repeat interval in millis

sealed class HRResult {
    data class HeartRateValue(val heartRate: Int) : HRResult()
    object StartedReading : HRResult()
    object Disconnected : HRResult()
}

enum class SyncStatus {
    Unknown, Running, Completed
}

@ExperimentalCoroutinesApi
fun SDKManager.startHeartRateFlow(): Flow<HRResult> =
        callbackFlow {
            var isHRTriggered = false
            var synStatus = SyncStatus.Completed
            val callback = object : SDKCallback {
                override fun onConnectionStateChanged(state: Int) {
                    if (BluetoothProfile.STATE_CONNECTED != state) {
                        sdk.userModule.setHeartRateTest(MSDefinition.HEART_RATE_TEST_STOP)
                        isHRTriggered = false
                        synStatus = SyncStatus.Unknown
                        offer(HRResult.Disconnected)
                    }
                }

                override fun onHeartRateChanged(hr: Int) {
                    sdk.userModule.setHeartRateTest(MSDefinition.HEART_RATE_TEST_STOP)
                    isHRTriggered = false
                    Log.d(TAG, "Heart rate is $hr")
                    if(!channel.isClosedForSend) offer(HRResult.HeartRateValue(hr))
                }
                override fun onStartSync() {
                    synStatus = SyncStatus.Running
                }

                override fun onSyncStarted() {
                    synStatus = SyncStatus.Running
                }

                override fun onSyncEnd() {
                    synStatus = SyncStatus.Completed
                }
            }
            setHeartRateCallback(callback)
            val timerTask = fixedRateTimer(TAG, false, 0, HR_MS_INFUTURE) {

                if (!isHRTriggered && isActive &&
                        sdk.bleModule.connectionState == BluetoothProfile.STATE_CONNECTED &&
                        synStatus == SyncStatus.Completed) {
                    sdk.userModule.setHeartRateTest(MSDefinition.HEART_RATE_TEST_START)
                    isHRTriggered = true
                    offer(HRResult.StartedReading)
                } else if (!isActive) {
                    cancel()
                    channel.close()
                } else if (isHRTriggered) {
                    sdk.userModule.setHeartRateTest(MSDefinition.HEART_RATE_TEST_STOP)
                    isHRTriggered = false
                }

            }
            awaitClose {
                timerTask.cancel()
                sdk.userModule.setHeartRateTest(MSDefinition.HEART_RATE_TEST_STOP)
                channel.close()
            }
        }

sealed class ConnectionResult {
    data class HeartRateHistory(val heartRate: Int, val timestamp: Long) : ConnectionResult()
    data class BloodPressureHistory(val systolic: Int, val diastolic: Int, val timestamp: Long) : ConnectionResult()
    data class StateSteps(val state: Int, val steps: Int, val timestamp: Long) : ConnectionResult()
    data class FirmwareVersion(val version: String) : ConnectionResult()
    sealed class Connected : ConnectionResult() {
        object Initialized : Connected()
        object Synchronized : Connected()
        object SynchronizedHrBp : Connected()
    }
    object Connecting : ConnectionResult()
    object Disconnected : ConnectionResult()

}
@ExperimentalCoroutinesApi
fun SDKManager.reconnectAndSync(address: String) : Flow<ConnectionResult> =
        callbackFlow<ConnectionResult> {
            var synStatus = SyncStatus.Unknown
            var isConnected = false
            val callback = object : SDKConnectionCallback {
                override fun onConnectionStateChanged(state: Int?) {
                    when (state) {
                        BluetoothProfile.STATE_CONNECTED -> {
                            firmware.readBatteryLevel()
                            isConnected = true
                            offer(ConnectionResult.Connected.Initialized)
                        }
                        BluetoothProfile.STATE_CONNECTING -> offer(ConnectionResult.Connecting)
                        else -> {
                            offer(ConnectionResult.Disconnected)
                            isConnected = false
                            synStatus = SyncStatus.Unknown
                        }
                    }
                }

                override fun onFirmwareVersionRead(version: String?, pair: Boolean, hrBp: Boolean, oxygen: Boolean) {
                    version?.let { offer(ConnectionResult.FirmwareVersion(it)) }
                    ble.startConnectionFlow()
                }

                override fun onStateAndStepsChanged(state: Int?, steps: Int?) {
                    //steps?.let { offer(ConnectionResult.StateSteps(state ?: 0, it)) }
                }

                override fun onSyncHistories(address: String?, state: Int?, steps: Int?, start: Long?) {
                    synStatus = SyncStatus.Running
                }

                override fun onSyncCurrentState(address: String?, state: Int?, steps: Int?, start: Long?, far: Int?) {
                    synStatus = SyncStatus.Running
                    steps?.let { offer(ConnectionResult.StateSteps(state ?: 0, it, start ?: System.currentTimeMillis())) }
                }

                override fun onSyncEnd() {
                    synStatus = SyncStatus.Completed
                    offer(ConnectionResult.Connected.Synchronized)

                }

                override fun onStartSync() {
                    synStatus = SyncStatus.Running
                }

                override fun onHrSyncHistories(address: String?, hr: Int?, time: Long?) {
                    synStatus = SyncStatus.Running
                    offer(ConnectionResult.HeartRateHistory(hr ?: 0, time ?: System.currentTimeMillis()))
                }

                override fun onBpSyncHistories(address: String?, systolic: Int?, diastolic: Int?, time: Long?) {
                    synStatus = SyncStatus.Running
                    offer(ConnectionResult.BloodPressureHistory(systolic ?: 0, diastolic ?: 0, time ?: System.currentTimeMillis()))
                }

                override fun onHrBpSyncEnd() {
                    offer(ConnectionResult.Connected.SynchronizedHrBp)
                }

            }
            setConnectionCallback(callback)
            ble.startReConnect(address)
            val timerTask = fixedRateTimer(CONN_TAG, false, 0, BAND_CHECK_MS_INFUTURE) {
                if (synStatus == SyncStatus.Completed && isConnected) {

                } else if (synStatus != SyncStatus.Completed) {
                    ble.requestSync()
                }

            }
            awaitClose {
                device_address = null
                timerTask.cancel()
                channel.close()
            }
        }