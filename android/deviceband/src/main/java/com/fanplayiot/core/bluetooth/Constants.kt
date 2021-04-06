package com.fanplayiot.core.deviceband.bluetooth

//Broadcast notice
const val FOUND_DEVICE = "com.mcube.ms.sdk.demo.found_device" //Scan/discover devices

const val DEVICE_RSSI = "com.mcube.ms.sdk.demo.device_rssi" //Signal strength of device

const val DEVICE_CONNECT_STATE = "com.mcube.sdk.demo.device_state" //Set connection status

const val DEVICE_FIRMWARE = "com.mcube.sdk.demo.device_firmware" // Device in firmware version

const val DEVICE_BATTERY = "com.mcube.sdk.demo.device_battery" //Device Battery Status and charge

const val DEVICE_MOTION_CHANGE = "com.mcubee.sdk.demo.devic_motion" // Device Status

const val SELFIE = "com.mcube.sdk.demo.selfie" //Selfie

const val SEDENTARYCHANGED = "com.mcube.sdk.demo.sedentary" //Sedentary

const val HEART_RATE = "com.mcube.sdk.demo.heart_rate" //HR

const val BLOOD_PRESSURE = "com.mcube.sdk.demo.blood_pressure" //BP

const val SYNC_HISTORY = "com.mcube.sdk.demo.sync_history"
const val SYNC_STATE = "com.mcube.sdk.demo.sync_state"
const val SYNC_HR = "com.mcube.sdk.demo.sync_hr"
const val SYNC_BP = "com.mcube.sdk.demo.sync_bp"
const val VAST_ALARM_NAME_SET = "com.mcube.sdk.demo.vast_alarm_name_set"
const val VAST_ALARM_TIME_SET = "com.mcube.sdk.demo.vast_alarm_time_set"
const val OTA_CHECK = "com.mcube.sdk.demo.ota_check"
const val WERUN = "com.mcube.sdk.demo.werun"
const val BLOOD_OXYGEN = "com.mcube.sdk.demo.blood_oxygen"
const val MEDICINE_ALARM = "com.mcube.sdk.demo.medicine_alarm"


//Broadcast parameter passing

//Broadcast parameter passing
const val device_name = "device_name"
const val device_address = "device_address"
const val device_rssi = "device_rssi"
const val connected = "connected"
const val firmware_version = "firmware_version"
const val pair = "pair"
const val hrBP = "hrBP"
const val battery = "battery"
const val battery_state = "battery_state"
const val device_motion_state = "device_motion_state"
const val device_steps = "device_steps"
const val rate = "rate"
const val systolic = "systolic"
const val diastolic = "diastolic"

const val sync_address = "address"
const val sync_state = "sync_state"
const val steps = "steps"
const val sync_start = "sync_start"
const val sync_far = "sync_far"
const val sync_end = "sync_end"
const val sync_time = "sync_time"
const val success = "success"
const val upgrade = "upgrade"
const val downloaded = "ota_downloaded"
const val ota_start = "ota_start"
const val ota_process = "ota_process"
const val ota_end = "ota_end"
const val werun_url = "werun_url"
const val blood_oxygen = "blood_oxygen"

// Fast Ble Name, UUIDs
const val NAME = "FAN GURU"
const val CONST_SERVICE_ID = "00005500-d102-11e1-9b23-00025b00a5a5"
const val CONST_CHARAC_ID = "00005501-d102-11e1-9b23-00025b00a5a5"
