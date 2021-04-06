package com.fanplayiot.core.ui.devices

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.clj.fastble.data.BleDevice
import com.fanplayiot.core.bluetooth.DeviceConnectListener
import com.fanplayiot.core.bluetooth.DeviceScanListener
import com.fanplayiot.core.bluetooth.FastBleManager
import com.fanplayiot.core.bluetooth.SDKManager
import com.fanplayiot.core.db.local.entity.Device
import com.fanplayiot.core.db.local.entity.DeviceType
import com.fanplayiot.core.db.local.repository.DevicesRepository
import com.fanplayiot.core.utils.Constant

class DevicesViewModel(application: Application) : AndroidViewModel(application), DeviceScanListener, DeviceConnectListener {
    private val repository: DevicesRepository
    private val manager: SDKManager
    private val fastBleManager: FastBleManager
    @JvmField
    var bandLive: LiveData<Device?>
    @JvmField
    var emoteLive: LiveData<Device?>
    @JvmField
    var bleDeviceLive = MutableLiveData<BleDevice?>()
    @JvmField
    var stateLive = MutableLiveData<FastBleManager.State>()
    @JvmField
    var showProgress = MutableLiveData(true)
    @JvmField
    var showDeletedMessage: MutableLiveData<Device?> = MutableLiveData(null)


    init {
        repository = DevicesRepository(application)
        manager = SDKManager.instance(application)
        fastBleManager = FastBleManager(getApplication())
        bandLive = repository.bandLive
        emoteLive = repository.emoteLive
    }

    fun scan() {
        // scan device for 15 seconds
        manager.ble.stopReConnect()
        manager.device_address = null
        manager.ble.scan(15)
        fastBleManager.startScan(this)
    }

    fun stopScan() {
        manager.ble.stopScan()
        fastBleManager.disconnectAll()
    }

    val isConnected: Boolean
        get() = manager.ble.connectionState == BluetoothProfile.STATE_CONNECTED

    fun getName(address: String?): String {
        val device: BluetoothDevice? = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address)
        return device?.name ?: "UnKnown"
    }

    val mACAddress: String
        get() = manager.ble.connectedDevice.address

    fun readBatteryLevel() {
        manager.firmware.readBatteryLevel()
    }

    fun insertBand(deviceAddress: String) {
        repository.insertDevice(Device(deviceAddress, Device.DEVICE_BAND, System.currentTimeMillis()))
    }

    fun insertEmote(deviceAddress: String) {
        repository.insertDevice(Device(deviceAddress, Device.DEVICE_EMOTE, System.currentTimeMillis()))
    }

    fun connect(address: String, @DeviceType type: Int) {
        manager.ble.stopScan()
        when (type) {
            Device.DEVICE_BAND -> {
                manager.ble.stopReConnect()
                manager.ble.connect(address)
            }
            Device.DEVICE_EMOTE -> {
                if (bleDeviceLive.value != null) {
                    Log.d(TAG, "bleDeviceLive ${bleDeviceLive.value!!.mac}")
                    fastBleManager.connect(bleDeviceLive.value, this)
                }
            }
        }
    }

    fun disconnect(address: String, @DeviceType type: Int) {
        when (type) {
            Device.DEVICE_BAND -> {
                manager.ble.stopReConnect()
                manager.ble.disconnect()
                manager.device_address = null
                repository.deleteDevice(address)
            }
            Device.DEVICE_EMOTE -> {
                fastBleManager.disconnectAll()
                repository.deleteDevice(address)
            }
        }
        showDeletedMessage.postValue(Device(address, type, 0L))
    }

    override fun onScanFinished(isSuccess: Boolean, scanResultList: List<BleDevice>) {
        if (!isSuccess) {
            stateLive.postValue(FastBleManager.State.SCAN_FAILED)
        } else {
            stateLive.postValue(FastBleManager.State.DEVICE_FOUND)
        }
    }

    override fun onScanning(bleDevice: BleDevice) {
        val name = bleDevice.name
        //val address = bleDevice.mac
        //Log.d(TAG, "on Scanning $name")
        if (Constant.NAME == name) {
            Log.d(TAG, "found device $name")
            bleDeviceLive.postValue(bleDevice)
        }
    }

    override fun onConnectFail() {
        stateLive.postValue(FastBleManager.State.CONN_FAILED)
    }

    override fun onConnectSuccess(bleDevice: BleDevice) {
        Log.d(TAG, "Connection success")
        bleDeviceLive.postValue(bleDevice)
        stateLive.postValue(FastBleManager.State.CONNECTED)
    }

    companion object {
        private const val TAG = "DevicesViewModel"
    }

}