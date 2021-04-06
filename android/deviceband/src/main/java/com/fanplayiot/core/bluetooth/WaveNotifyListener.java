package com.fanplayiot.core.bluetooth;

import com.clj.fastble.data.BleDevice;

public interface WaveNotifyListener {
    void onConnectFail();
    void onConnectSuccess(BleDevice bleDevice);
    void onDisconnect();
    void onUpdateWave(int count);
    void invokeWhistle();
}
