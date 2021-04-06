package com.fanplayiot.core.bluetooth;

import com.clj.fastble.data.BleDevice;

import java.util.List;

public interface DeviceScanListener {
    void onScanFinished(boolean isSuccess, List<BleDevice> scanResultList);
    void onScanning(BleDevice bleDevice);
}
