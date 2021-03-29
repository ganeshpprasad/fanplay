//
//  MCBLEPublicHeader.h
//  MCBLESDK
//
//  Created by Nick Yang on 2017/10/11.
//  Copyright © 2017 mCube. All rights reserved.
//

#ifndef MCBLEPublicHeader_h
#define MCBLEPublicHeader_h

@import CoreBluetooth;
@import CoreGraphics;
#import "MCBLEPublicEnum.h"

#import "MCUserProfileInfo.h"
#import "MCBLEDisplaySettingInfo.h"
#import "MCBLEAlarmSettingInfo.h"
#import "MCBLEDeviceInfo.h"
#import "MCBLEFirmwareVersion.h"
#import "MCBLEMotionSyncData.h"
#import "MCBLEOfflineSyncData.h"
#import "MCPaceRange.h"

/*
 Notification keywords，
 If there is return data, use NSNotification.object to return.
 */
//Bluetooth state update
extern NSString * const MCBLENotificationManagerStateDidUpdate;

//Connect succeeded, return MCBLEDeviceInfo.
extern NSString * const MCBLENotificationDeviceConnectSucceeded;

//Connect failed，return MCBLEDeviceInfo.
extern NSString * const MCBLENotificationDeviceConnectFailed;

//Connection break, return MCBLEDeviceInfo.
extern NSString * const MCBLENotificationDeviceDidDisconnect;

//Data synchronization completed, return NSNumber.
extern NSString * const MCBLENotificationDataSyncDidComplete;

//Shake to photo, with no return data.
extern NSString * const MCBLENotificationDeviceSelfieDidDetect;

//Sedentary reminder, with no return data.
extern NSString * const MCBLENotificationDeviceSedentaryRemind;

//Find phone, with no return data.
extern NSString * const MCBLENotificationFindPhone;

//Battery information update, return MCBLEDeviceInfo.
extern NSString * const MCBLENotificationDeviceBatteryDidUpdate;

//Firmware version read, return MCBLEDeviceInfo.
extern NSString * const MCBLENotificationDeviceFirmwareVersionDidRead;

//Device Mac address read, return MCBLEDeviceInfo.
extern NSString * const MCBLENotificationDeviceMacAddressDidRead;

//Heart rate measured, return MCBLEOfflineSyncData.
extern NSString * const MCBLENotificationDeviceHeartRateMeasureComplete;

//Blood pressure measured, return MCBLEOfflineSyncData.
extern NSString * const MCBLENotificationDeviceBloodPressureMeasureComplete;

//Device real-time data update, return MCBLEMotionSyncData.
extern NSString * const MCBLENotificationDeviceRealTimeDataDidUpdate;

/*
 Constants
 */
//Heart rate measurement duration: 50s.
extern CGFloat const MCBLEDeviceHeartRateMeasureDuration;
//Blood pressure measurement duration: 50s.
extern CGFloat const MCBLEDeviceBloodPressureMeasureDuration;

//SpO2 measured, return MCBLEOfflineSyncData.
extern NSString * const MCBLENotificationDeviceSPO2MeasureComplete;
//SpO2 measurement duration 50s.
extern CGFloat const MCBLEDeviceSPO2MeasureDuration;

/*
 Bluetooth connection protocol
 */
@class MCBLEDeviceInfo, MCBLEMotionSyncData, MCBLEOfflineSyncData;
@protocol MCBLESDKConnectDelegate <NSObject>
@optional
//Bluetooth state update.
- (void)onManagerStateUpdate:(MCManagerState)managerState;
//Scan complete (Timer timeout).
- (void)onScanEnd;
//Detect a device.
- (void)onDiscoverDevice:(MCBLEDeviceInfo *)device;
//Connect succeed.
- (void)onDeviceConnectSucceeded:(MCBLEDeviceInfo *)device;
//Connect failed.
- (void)onDeviceConnectFailed:(MCBLEDeviceInfo *)device;
//Device did disconnect.
- (void)onDeviceDisconnect:(MCBLEDeviceInfo *)device;
@end


/*
 Device ota protocol
 */
@protocol MCBLESDKOTADelegate <NSObject>
@optional
//Updated firmware detection completed. if file is nil. it means no updated firmware or network error. Otherwise it is the target file path.
- (void)onOTACheckComplete:(NSString *)file;
//Specified firmware binary file download complete, and its format is NSData *，if fileData is nil，it means downloading failed.
- (void)onOTAFileDownloaded:(NSData *)fileData;
//OTA process (progress value range is 0.0~1.0).
- (void)onOTAProcess:(CGFloat)progress;
//OTA end
- (void)onOTAEnd:(BOOL)succeed;
@end


/*
 Device data synchronize protocol
 */
@protocol MCBLESDKDataSyncDelegate <NSObject>
//Motion base time.
- (void)onSyncMotionDataBaseTime:(MCBLEMotionSyncData *)data;
//Motion record.
- (void)onSyncMotionDataRecords:(NSArray <MCBLEMotionSyncData *> *)records;
//Motion current state.
- (void)onSyncMotionDataCurrentState:(MCBLEMotionSyncData *)data;
//Data synchronize end.
- (void)onSyncEnd:(BOOL)succeed;

@optional
//Base time of off-line record (Heart rate/blood pressure).
- (void)onSyncOfflineDataBaseTime:(MCBLEOfflineSyncData *)data;
//Off-line record (Heart rate/blood pressure).
- (void)onSyncOfflineDataRecords:(NSArray <MCBLEOfflineSyncData *> *)records;
@end

#endif /* MCBLEPublicHeader_h */
