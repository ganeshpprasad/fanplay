package com.fanplayiot.core.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.fanplayiot.core.deviceband.bluetooth.ConstantsKt;
import com.mcube.ms.sdk.MSSDK;
import com.mcube.ms.sdk.definitions.MSDefinition;
import com.mcube.ms.sdk.interfaces.MSCallbacks;
import com.mcube.ms.sdk.modules.BLEModule;
import com.mcube.ms.sdk.modules.FirmwareModule;
import com.mcube.ms.sdk.modules.OTAModule;
import com.mcube.ms.sdk.modules.SportModule;
import com.mcube.ms.sdk.modules.UserModule;


public class SDKManager implements MSCallbacks
{
    public Integer batPercent;
    private int steps;
    private String calorie;
    private String distance;
    public MSSDK sdk;
    public String device_address;
    private static final String TAG = SDKManager.class.getSimpleName();

    private static SDKManager manager;
    private SDKCallback heartRateCallback;
    private SDKConnectionCallback connectionCallback;
    private Context context;

    public static SDKManager instance(@NonNull Context context)
    {
        if(manager == null)
            manager = new SDKManager(context);
        return manager;
    }

    public BLEModule getBLE()
    {
        return sdk.getBLEModule();
    }

    public FirmwareModule getFirmware()
    {
        return sdk.getFirmwareModule();
    }

    public UserModule getUserModule()
    {
        return sdk.getUserModule();
    }

    public SportModule getSportModule()
    {
        return sdk.getSportModule();
    }
    public OTAModule getOTAModule(){return sdk.getOTAModule();}


    private SDKManager(@NonNull Context context)
    {
        this.context = context;
        sdk = new MSSDK(context);
        //设置调试模式
        sdk.setDebugEnable(true);
        //设置回调
        sdk.setMSCallbacks(this);

        //参数设置

    }

    public void setHeartRateCallback(SDKCallback heartRateCallback) {
        this.heartRateCallback = heartRateCallback;
    }

    public void setConnectionCallback(SDKConnectionCallback connectionCallback) {
        this.connectionCallback = connectionCallback;
    }

    @Override
    public void onConnectionStateChanged(int state)
    {

        Log.i("TAG",BluetoothProfile.STATE_CONNECTED == state?"Device Connected":"Device Disconnected");
        Intent intent = new Intent(ConstantsKt.DEVICE_CONNECT_STATE);
        intent.putExtra(ConstantsKt.connected,BluetoothProfile.STATE_CONNECTED == state);
        context.sendBroadcast(intent);

        //断开连接重连
        if(state == BluetoothProfile.STATE_DISCONNECTED && device_address != null)
            sdk.getBLEModule().startReConnect(device_address);

        else if(state == BluetoothProfile.STATE_CONNECTED)
        {
            device_address = sdk.getBLEModule().getConnectedDevice().getAddress();
            sdk.getBLEModule().setBatteryNotification(true);
        }

        if (heartRateCallback != null) heartRateCallback.onConnectionStateChanged(state);
        if (connectionCallback != null) connectionCallback.onConnectionStateChanged(state);
    }

    @Override
    public void onServicesDiscovered(int status, boolean werun)
    {
        Log.i(TAG, "onServicesDiscovered() - status = " + status + ", support Werun = " + werun);
    }

    @Override
    public void onRSSIRead(int rssi)
    {
        Log.i(TAG,"Device rssi: "+rssi);
        Intent intent = new Intent(ConstantsKt.DEVICE_RSSI);
        intent.putExtra(ConstantsKt.device_rssi,rssi);

        context.sendBroadcast(intent);
    }

    @Override
    public void onDeviceScanned(BluetoothDevice device) {


        Log.i(TAG,"Found device: "+ device.getName()+" address: "+device.getAddress());
        Intent intent = new Intent(ConstantsKt.FOUND_DEVICE);
        intent.putExtra(ConstantsKt.device_name,device.getName() == null?"NULL":device.getName());
        intent.putExtra(ConstantsKt.device_address,device.getAddress());
        context.sendBroadcast(intent);

    }

    @Override
    public void onFirmwareVersionRead(String version, boolean pair, boolean hrBp,boolean oxygen) {

        Log.i(TAG,"firmware : "+version +" need pair: "+pair+" suport hear rate and BP: "+hrBp +" suport blood oxgeng: "+oxygen);

        Intent intent = new Intent(ConstantsKt.DEVICE_FIRMWARE);
        intent.putExtra(ConstantsKt.firmware_version,version);
        intent.putExtra(ConstantsKt.pair,pair);
        intent.putExtra(ConstantsKt.hrBP,hrBp);
        intent.putExtra(ConstantsKt.blood_oxygen,oxygen);
        context.sendBroadcast(intent);
        if (connectionCallback != null) connectionCallback.onFirmwareVersionRead(
                version, pair, hrBp, oxygen);
    }

    @Override
    public void onBatteryRead(int percentage, int state) {


        String state_text = null;

        switch (state)
        {
            case 0:
                state_text = "Charging";
                break;
            case 1:
                state_text = "Charged";
                break;
            default:
                state_text = "Not charging";
                break;
        }
        Log.i(TAG,"Battery: "+percentage +"%"+" "+state_text);

        Intent intent = new Intent(ConstantsKt.DEVICE_BATTERY);
        intent.putExtra(ConstantsKt.battery,percentage);
        intent.putExtra(ConstantsKt.battery_state,state_text);
        context.sendBroadcast(intent);
        batPercent = percentage;

    }

    @Override
    public void onStateAndStepsChanged(int state, int steps) {

        String[] array = new String[] {"Stationary","Walking","Running","Sleep","Awake"," Restless"};
        Log.i(TAG,"onStateAndStepsChanged: "+array[state]+" steps: "+steps);
        String text = array[state];
        Intent intent = new Intent(ConstantsKt.DEVICE_MOTION_CHANGE);
        intent.putExtra(ConstantsKt.device_motion_state,text);
        intent.putExtra(ConstantsKt.device_steps,steps);
        context.sendBroadcast(intent);
        if (connectionCallback != null) connectionCallback.onStateAndStepsChanged(state, steps);
    }

    @Override
    public void onSelfieChanged() {

        Log.i(TAG,"SelfieChanged");
        Intent intent = new Intent(ConstantsKt.SELFIE);
        context.sendBroadcast(intent);

    }

    @Override
    public void onSedentaryChanged() {
        Log.i(TAG,"onSedentaryChanged");
        Intent intent = new Intent(ConstantsKt.SEDENTARYCHANGED);
        context.sendBroadcast(intent);
    }

    @Override
    public void onHeartRateChanged(int hr) {
        Log.i(TAG,"onHeartRateChanged");
        Intent intent = new Intent(ConstantsKt.HEART_RATE);
        intent.putExtra(ConstantsKt.rate,hr);
        context.sendBroadcast(intent);
        if (heartRateCallback != null) heartRateCallback.onHeartRateChanged(hr);
    }

    @Override
    public void onBloodPressureChanged(int systolic, int diastolic) {

        Log.i(TAG,"BloodPressure - systolic: "+systolic+" diastolic: "+diastolic);
        Intent intent = new Intent(ConstantsKt.BLOOD_PRESSURE);
        intent.putExtra(ConstantsKt.systolic,systolic);
        intent.putExtra(ConstantsKt.diastolic,diastolic);
        context.sendBroadcast(intent);
    }

    @Override
    public void onBloodOxygen(int percent)
    {
        Log.i(TAG,"Blood Oxygen percent: "+percent);
        Intent intent = new Intent(ConstantsKt.BLOOD_OXYGEN);
        intent.putExtra(ConstantsKt.blood_oxygen,percent);
        context.sendBroadcast(intent);
    }

    @Override
    public void onSyncHistories(String address, int state, int steps, long start) {

        Log.i(TAG,"sync history - address: "+address +" state: "+state+" steps: "+steps+" start:"+start);
        if(address == null)
            return;
        Intent intent  = new Intent(ConstantsKt.SYNC_HISTORY);
        intent.putExtra(ConstantsKt.sync_address,address);
        intent.putExtra(ConstantsKt.sync_start,start);
        intent.putExtra(ConstantsKt.sync_state,state);
        intent.putExtra(ConstantsKt.steps,steps);

        context.sendBroadcast(intent);
        if (heartRateCallback != null) heartRateCallback.onStartSync();
        if (connectionCallback != null) connectionCallback.onSyncHistories(address, state ,steps , start);
    }

    @Override
    public void onBloodOxygenHistories(String address, int oxygen, long time) {
        Log.i(TAG,"sync history - address: "+address +" oxygen: "+oxygen+" steps: "+" time:"+time);
        if(address == null)
            return;
        Intent intent  = new Intent(ConstantsKt.SYNC_HISTORY);
        intent.putExtra(ConstantsKt.sync_address,address);
        intent.putExtra(ConstantsKt.sync_start,time);
        intent.putExtra(ConstantsKt.blood_oxygen,oxygen);

        context.sendBroadcast(intent);
        if (heartRateCallback != null) heartRateCallback.onStartSync();
    }

    @Override
    public void onSyncCurrentState(String address, int state, int steps, long start, int far) {

        Log.i(TAG,"sync current state - address "+address+" state:"+state+" steps:"+steps+" start:"+start+" far:"+far);
        setSteps(steps);
        if(address == null)
            return;
        Intent intent = new Intent(ConstantsKt.SYNC_STATE);
        intent.putExtra(ConstantsKt.sync_address,address);
        intent.putExtra(ConstantsKt.sync_state,state);
        intent.putExtra(ConstantsKt.steps,steps);
        intent.putExtra(ConstantsKt.sync_start,start);
        intent.putExtra(ConstantsKt.sync_far,far);

        context.sendBroadcast(intent);
        if (heartRateCallback != null) heartRateCallback.onStartSync();
        if (connectionCallback != null) connectionCallback.onSyncCurrentState(address, state, steps, start, far);
    }



    @Override
    public void onSyncEnd() {

        Log.i(TAG,"Sync End");
        Intent intent = new Intent(ConstantsKt.SYNC_STATE);
        intent.putExtra(ConstantsKt.sync_end,true);
        context.sendBroadcast(intent);

        if (heartRateCallback != null) heartRateCallback.onStartSync();
        if (connectionCallback != null) connectionCallback.onSyncEnd();
    }

    @Override
    public void onStartSync() {
        Log.i(TAG,"onStartSync");
        if (heartRateCallback != null) heartRateCallback.onStartSync();
        if (connectionCallback != null) connectionCallback.onStartSync();
    }

    @Override
    public void onHrSyncHistories(String address, int hr, long time) {

        Log.i(TAG,"sync history heart rate : "+hr+" time:" +time);
        Intent intent = new Intent(ConstantsKt.SYNC_HR);
        intent.putExtra(ConstantsKt.rate,hr);
        intent.putExtra(ConstantsKt.sync_time,time);

        context.sendBroadcast(intent);

        if (heartRateCallback != null) heartRateCallback.onStartSync();
        if (connectionCallback != null) connectionCallback.onHrSyncHistories(address, hr, time);
    }

    @Override
    public void onBpSyncHistories(String address, int systolic, int diastolic, long time) {
        Log.i(TAG,"sync history heart BP - systolic: "+systolic+" diastolic:"+diastolic+" time:" +time);
        Intent intent = new Intent(ConstantsKt.SYNC_BP);
        intent.putExtra(ConstantsKt.systolic,systolic);
        intent.putExtra(ConstantsKt.diastolic,diastolic);
        intent.putExtra(ConstantsKt.sync_time,time);

        context.sendBroadcast(intent);
        if (heartRateCallback != null) heartRateCallback.onStartSync();
        if (connectionCallback != null) connectionCallback.onBpSyncHistories(address, systolic, diastolic, time);
    }

    @Override
    public void onHrBpSyncEnd() {

        Log.i(TAG,"heart rate and BP sync end");
        Intent intent = new Intent(ConstantsKt.SYNC_STATE);
        intent.putExtra(ConstantsKt.sync_end,true);
        context.sendBroadcast(intent);

        if (heartRateCallback != null) heartRateCallback.onSyncEnd();
        if (connectionCallback != null) connectionCallback.onHrBpSyncEnd();
    }

    @Override
    public void onVastAlarmNameSet(boolean success) {
        Log.i(TAG,"set Vast Alarm Name :"+success);
        Intent intent = new Intent(ConstantsKt.VAST_ALARM_NAME_SET);
        intent.putExtra(ConstantsKt.success,success);
        context.sendBroadcast(intent);
    }

    @Override
    public void onVastAlarmTimeSet(boolean success) {
        Log.i(TAG,"set Valst Alarm Time: "+success);
        Intent intent = new Intent(ConstantsKt.VAST_ALARM_TIME_SET);
        intent.putExtra(ConstantsKt.success,success);
        context.sendBroadcast(intent);
    }

    @Override
    public void onOTAChecked(boolean upgrade) {
        Log.i(TAG,"OTA Checked: "+upgrade);
        Intent intent = new Intent(ConstantsKt.OTA_CHECK);
        intent.putExtra(ConstantsKt.upgrade,upgrade?1:0);
        context.sendBroadcast(intent);
    }

    @Override
    public void onOTADownloaded(boolean success) {
        Log.i(TAG,"OTA Downloaded: "+success);
        Intent intent = new Intent(ConstantsKt.OTA_CHECK);
        intent.putExtra(ConstantsKt.downloaded,success);
        context.sendBroadcast(intent);
    }

    @Override
    public void onOTAStart() {
        Log.i(TAG,"OTA Start");
        Intent intent = new Intent(ConstantsKt.OTA_CHECK);
        intent.putExtra(ConstantsKt.ota_start,true);
        context.sendBroadcast(intent);
    }

    @Override
    public void onOTAProcess( float percent) {
        Log.i(TAG,"OTA Process " + percent);
        Intent intent = new Intent(ConstantsKt.OTA_CHECK);
        intent.putExtra(ConstantsKt.ota_process,true);
        context.sendBroadcast(intent);
    }

    @Override
    public void onOTAEnd() {
        Log.i(TAG,"OTA End");
        Intent intent = new Intent(ConstantsKt.OTA_CHECK);
        intent.putExtra(ConstantsKt.ota_end,true);
        context.sendBroadcast(intent);
    }

    @Override
    public void onWeRunConnected(boolean connected, String qr) {
        Log.i(TAG,"we run");
        Intent intent = new Intent(ConstantsKt.WERUN);
        intent.putExtra(ConstantsKt.werun_url,qr);
        context.sendBroadcast(intent);
    }

    @Override
    public void onMedicineSet(boolean success) {
        Log.i(TAG,"Medicine Set "+success);
        Intent intent = new Intent(ConstantsKt.MEDICINE_ALARM);
        intent.putExtra(ConstantsKt.success,success);
        context.sendBroadcast(intent);
    }

    private void setSteps(int s)
    {
        this.steps = s;
        int uint =context.getSharedPreferences("profile", Context.MODE_PRIVATE).getInt("unit", MSDefinition.UNIT_KILOMETERS);
        calorie = (this.getSportModule().getCalorie(steps));
        distance = getSportModule().getDistance(steps,uint) + (uint == MSDefinition.UNIT_KILOMETERS?"km":"ml");
    }

    public int getSteps(){return steps;}
    public String getCalorie(){return calorie;}
    public String getDistance(){return distance;}
}
