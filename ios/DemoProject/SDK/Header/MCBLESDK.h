//
//  MCBLESDK.h
//  MCBLESDK
//
//  Created by Nick Yang on 2017/10/11.
//  Copyright © 2017 mCube. All rights reserved.
//
//  Version 1.2.4
//

#import "MCBLEPublicHeader.h"

@interface MCBLESDK : NSObject

//Get the current version of the SDK.
+ (NSString *)SDKVersion;

//Debug mode settings. it will output detailed log information in the console when be turned on. The default is disable.
+ (void)setDebugMode:(BOOL)enabled;

//Set delegate
+ (void)setConnectDelegate:(id<MCBLESDKConnectDelegate>)connectDelegate;
+ (void)setOtaDelegate:(id<MCBLESDKOTADelegate>)otaDelegate;
+ (void)setSyncDelegate:(id<MCBLESDKDataSyncDelegate>)syncDelegate;

//Device supported features.
+ (MCBLEDeviceFeatureMode)deviceFeatureMode;

//Whether to support device time format configuration.
+ (BOOL)timeFormatConfigEnable;

//Whether to support display settings.
+ (BOOL)displaySettingsEnable;

//Does ANCS support Facebook, Twitter, WhatsApp, Instagram?
+ (BOOL)ancsForFacebookEnable;

//Whether to support access to WeChat campaign (streamline protocol, AirSync protocol), need to judge in the connected state
+ (BOOL)accessWerunEnable;

//Whether to support SpO2 measurement, need to judge in the connected state.
+ (BOOL)SPO2FeatureEnable;

/*
 Distance, calories related.
 Distance unit is km or mi.
 See detail in file MCUserProfileInfo.h
 */
//Whether the distance unit is km, else is mi.
+ (BOOL)distanceUnitIsKM;

//Distance calculate through step count.
+ (CGFloat)distanceWithStep:(NSUInteger)step;

//Calorie calculate through step count. Calorie unit is kCal.
+ (CGFloat)calorieWithStep:(NSUInteger)step;

/*
 Return distance floating point value string, without unit.
 Return value retains two digits after the decimal point.
 */
+ (NSString *)distanceStringWithStep:(NSUInteger)step;

/*
 Return calorie value string, without unit.
 Return the integer value.
 */
+ (NSString *)calorieStringWithStep:(NSUInteger)step;

//Bluetooth state.
+ (MCManagerState)managerState;
+ (BOOL)managerStateIsPoweredOn;

/*
 Marking the process phase of the current device connection，is different from CBPeripheralState functionality. See detail in file MCBLEPublicEnum.h.
 */
+ (MCBLEConnectionState)connectionState;
+ (BOOL)connectionStateIsConnected;

/*
 Get the device that is currently connecting or connected, and may return nil.
 */
+ (MCBLEDeviceInfo *)currentDevice;

//Scan devices function (default duartion is 10s).
+ (void)scan;

//Scan devices using specified duration.
+ (void)scan:(NSTimeInterval)duration;

//Manual termination of search device，it will not trigger this callback: - (void)onScanEnd;
+ (void)stopScan;

/*
 The SDK only supports the connection of a single Bluetooth device. If a new device connection is initiated, the previously connected device will be disconnected automatically.
 Single connection mode: call once connection once, not automatically reconnect.
 */
+ (void)connect:(MCBLEDeviceInfo *)device;

//Actively disconnect current connected devices.
+ (void)disconnect;


/*
 The following is the operation of the currently connected device.
 */

//Read the peripheral battery information.
+ (void)readBattery;

//Switch device battery information update broadcasting function.
+ (void)subscribeBatteryNotify:(BOOL)enable;

//Whether the CBCharacteristic is subscripted.
+ (BOOL)batteryCharacteristicIsNotifying;

//Read the firmware version of device.
+ (void)readFirmwareVersion;

//Read device real-time data.
+ (void)readRealTimeValue;

//Read device Mac address.
+ (BOOL)readMacAddress;

/*
 OTA function.
 */
+ (BOOL)otaProcessFlag;

//Input Bin data: if return value is NO, OTA start failed. Otherwise, succeed.
+ (BOOL)otaStartWithData:(NSData *)binData;

//OTA cancel action will interrupt data transmission, but it will not cause the device to terminate the OTA state, so additional manual disconnection is needed.
+ (void)otaCancel;

//Check updated firmware file on the server. 
+ (void)otaCheck:(MCBLEFirmwareVersion *)version;

 //Download specified firmware binary file using the file path parameter.
+ (void)otaDownloadFile:(NSString *)file;

//Data synchronization.
+ (void)syncData;

//When device connected, whether SDK will automaticallly call +(void)syncData. Default is NO.
+ (void)setAutoSyncDataWhenConnected:(BOOL)enable;

//Look for the device.
+ (void)findWristband;

//Set the device time. the parameter “date” is the specified time, and the current system time is used when nil is passed in.
+ (void)setTime:(NSDate *)date;

//User Information Settings.
+ (void)setProfile:(MCUserProfileInfo *)info;

//Device Display Settings.
+ (void)setDisplay:(MCBLEDisplaySettingInfo *)info;

//Alarm clock settings.
+ (void)setSmartAlarm:(MCBLEAlarmSettingInfo *)info;

//Shake to photograph function settings.
+ (void)setSelfieDetection:(BOOL)enable;

//Heart rate measurement function settings.
+ (void)setHeartRateMeasure:(BOOL)enable;

//Blood pressure measurement function settings.
+ (void)setBloodPressureMeasure:(BOOL)enable;

//SpO2 measurement function settings.
+ (void)setSPO2Measure:(BOOL)enable;

//Incomming call reminder settings.
+ (void)setIncomingCallRemind:(BOOL)enable;

//ANCS reminder settings, inclue: sms, QQ, wechat (New firmware version support Facebook，Twitter, WhatsApp, Instagram, etc.).
+ (void)setAncsFeatureRemind:(BOOL)enable;

//Vibration reminder settings.
+ (void)setVibrateRemind:(BOOL)enable;

//Anti-lost reminder settings.
+ (void)setAntiLostRemind:(BOOL)enable;

/*
 Sedentary reminder settings.
 minutes: NSInteger, range(60, 90, 120).
 */
+ (void)setSedentaryRemind:(BOOL)enable interval:(NSInteger)minutes;

@end

