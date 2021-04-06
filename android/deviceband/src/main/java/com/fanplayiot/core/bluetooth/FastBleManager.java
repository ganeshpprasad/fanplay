package com.fanplayiot.core.bluetooth;

import android.app.Application;
import android.bluetooth.BluetoothGatt;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.data.BleScanState;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.clj.fastble.utils.HexUtil;

import java.util.List;

import static com.fanplayiot.core.deviceband.bluetooth.ConstantsKt.CONST_CHARAC_ID;
import static com.fanplayiot.core.deviceband.bluetooth.ConstantsKt.CONST_SERVICE_ID;
import static com.fanplayiot.core.deviceband.bluetooth.ConstantsKt.NAME;


public class FastBleManager {
    private static final String TAG = "FastBleManager";
    private static final long CONNECT_OVER_TIME = 60000L;
    private static final int OPERATE_TIMEOUT = 120000;
    private static final int RECONNECT_COUNT = 1;
    private static final int RECONNECT_INTERVAL = 5000;
    private static final int SCAN_TIMEOUT = 10000;
    public enum State {
        SCAN_FAILED,
        DEVICE_FOUND,
        CONNECTING,
        CONNECTED,
        CONN_FAILED
    }

    private static double prevAccel = 0;
    private Integer move = 0;
    private BleDevice bleDevice;
    private MutableLiveData<State> mStateLive = new MutableLiveData<>();

    public FastBleManager(Application application) {
        BleManager.getInstance().init(application);
        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(RECONNECT_COUNT, RECONNECT_INTERVAL)
                .setConnectOverTime(CONNECT_OVER_TIME)
                .setOperateTimeout(OPERATE_TIMEOUT);
    }

    public LiveData<State> getStateLive() {
        return mStateLive;
    }

    public void startScan(final DeviceScanListener listener) {
        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
                .setDeviceName(true, NAME).setAutoConnect(true).setScanTimeOut(SCAN_TIMEOUT)
                .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);
        startBleScan(listener);
    }

    private void startBleScan(final DeviceScanListener listener) {
        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                listener.onScanFinished(scanResultList != null && scanResultList.size() > 0,
                        scanResultList);
                Log.d(TAG, "onScanFinished ");
            }

            @Override
            public void onScanStarted(boolean success) {
                Log.d(TAG, "onScanStarted ");
            }

            @Override
            public void onScanning(BleDevice bleDevice) {
                listener.onScanning(bleDevice);
            }
        });
    }

    public void connect(final BleDevice bleDevice, final DeviceConnectListener listener) {
        if (!BleManager.getInstance().isConnected(bleDevice)) {
            BleManager.getInstance().cancelScan();
            connectDevice(bleDevice, listener);
        }
    }

    private void connectDevice(final BleDevice bleDevice, final DeviceConnectListener listener) {
        BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
            @Override
            public void onStartConnect() {
                Log.d(TAG, "onStartConnect ");
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                if (BleManager.getInstance().isConnected(bleDevice)) {
                    BleManager.getInstance().disconnect(bleDevice);
                }
                listener.onConnectFail();
                Log.d(TAG, "onConnectFail " + exception.toString());
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                BleManager.getInstance().disconnectAllDevice();
                listener.onConnectSuccess(bleDevice);
                Log.d(TAG, "onConnectSuccess " + status);
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
                Log.d(TAG, "onDisConnected isActiveDisConnected =" + isActiveDisConnected +
                        "\n Gatt services list " + gatt.getServices().size() +
                        "\n status " + status);
            }
        });
    }

    public void connectForMAC(@NonNull final String address, @NonNull final WaveNotifyListener listener) {
        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
                .setDeviceName(true, NAME).setAutoConnect(true)
                .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);
        BleManager.getInstance().setOperateTimeout(OPERATE_TIMEOUT).connect(address, new BleGattCallback() {
            @Override
            public void onStartConnect() {
                mStateLive.postValue(State.CONNECTING);
                Log.d(TAG, "onStartConnect ");
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                listener.onConnectFail();
                mStateLive.postValue(State.CONN_FAILED);
                Log.d(TAG, "onConnectFail " + exception.toString());
            }

            @Override
            public void onConnectSuccess(BleDevice device, BluetoothGatt gatt, int status) {
                listener.onConnectSuccess(device);
                setBleDevice(device);
                notifyWave(listener);
                mStateLive.postValue(State.CONNECTED);
                Log.d(TAG, "onConnectSuccess " + status);
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
                Log.d(TAG, "onDisConnected isActiveDisConnected =" + isActiveDisConnected +
                        "\n Gatt services list " + gatt.getServices().size() +
                        "\n status " + status);
                /*if (!BleManager.getInstance().isConnected(address)) {
                    connectForMAC(address, listener);
                }*/
                mStateLive.postValue(State.CONN_FAILED);
            }
        });
    }

    public void disconnectAll() {
        BleScanState scanState = BleManager.getInstance().getScanSate();
        if (scanState == BleScanState.STATE_SCANNING) {
            BleManager.getInstance().cancelScan();
        }
        BleManager.getInstance().disconnectAllDevice();
    }

    public void notifyWave(@NonNull final WaveNotifyListener listener) {
        move = 0;
        BleManager.getInstance()
                .notify(bleDevice, CONST_SERVICE_ID, CONST_CHARAC_ID, true,
                new BleNotifyCallback() {
                    @Override
                    public void onNotifySuccess() {
                        Log.d(TAG, "onNotifySuccess ");
                    }

                    @Override
                    public void onNotifyFailure(BleException exception) {
                        Log.d(TAG, "error  " + exception.toString());
                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        String hexString = HexUtil.formatHexString(data);
                        Log.d(TAG, "messages " + hexString);
                        if (hexString.equals("424d5354")) {
                            Log.d(TAG, "Whistle Podu");
                            listener.invokeWhistle();
                        } else {
                            String xvalue = hexString.substring(0, 4);
                            String yvalue = hexString.substring(4, 8);
                            String zvalue = hexString.substring(8, 12);
                            Integer xval = Integer.parseInt(xvalue, 16);
                            Integer yval = Integer.parseInt(yvalue, 16);
                            Integer zval = Integer.parseInt(zvalue, 16);
                            Double xvalsq = Math.pow(xval, 2);
                            Double yvalsq = (double) (yval * yval);
                            Double zalsq = (double) (zval * zval);
                            double accelerate = Math.sqrt(xvalsq + yvalsq + zalsq);

                           // Log.d(TAG, "acceleration " + accelerate);
                           // Log.d(TAG, "prev acceleration " + prevAccel);
                            Log.d(TAG,"chk movement: "+Math.abs(accelerate-prevAccel));
                            //trying to take the absolute value of difference to get displacement/acceleration in a particular plane
                            if (Math.abs(accelerate - prevAccel) > 0) {
                              //  Log.d(TAG,"movement: "+Math.abs(accelerate-prevAccel));
                                move++;
                                Log.d(TAG, "moved " + move);
                                // update the count by 1 , not move
                                listener.onUpdateWave(1);
                            }
                            prevAccel = accelerate;
                        }
                    }
                });
    }

    public void removeNotify(BleDevice bleDevice) {
        BleManager.getInstance().clearCharacterCallback(bleDevice);
    }

    public void setBleDevice(BleDevice bleDevice) {
        this.bleDevice = bleDevice;
    }
}
