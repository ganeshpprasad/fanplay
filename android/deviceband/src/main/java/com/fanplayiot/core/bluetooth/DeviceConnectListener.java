package com.fanplayiot.core.bluetooth;

import com.clj.fastble.data.BleDevice;

public interface DeviceConnectListener {
    void onConnectFail();
    void onConnectSuccess(BleDevice bleDevice);
}
