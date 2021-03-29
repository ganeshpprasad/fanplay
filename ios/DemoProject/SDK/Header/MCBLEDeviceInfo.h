//
//  MCBLEDeviceInfo.h
//  MCBLESDK
//
//  Created by Nick Yang on 2017/8/31.
//  Copyright © 2017 mCube. All rights reserved.
//

@import CoreBluetooth;

typedef NS_ENUM(NSInteger, MCBLEDeviceBatteryLevel)
{
    MCBLEDeviceBatteryLowPower,         //0%~19%
    MCBLEDeviceBatteryLevelPercent20,   //20%~29%
    MCBLEDeviceBatteryLevelPercent40,   //30%~49%
    MCBLEDeviceBatteryLevelPercent60,   //50%~69%
    MCBLEDeviceBatteryLevelPercent80,   //70%~89%
    MCBLEDeviceBatteryLevelPercent100,  //>=90%
};

@class MCBLEFirmwareVersion;
@interface MCBLEDeviceInfo : NSObject

@property (strong, nonatomic) CBPeripheral *peripheral;
@property (strong, nonatomic) MCBLEFirmwareVersion *firmwareVersion;
@property (strong, nonatomic, readonly) NSString *uuid;
@property (strong, nonatomic, readonly) NSString *macAddress;
@property (assign, nonatomic, readonly) NSInteger batteryValue;
@property (assign, nonatomic, readonly) BOOL chargeFlag;

@property (strong, nonatomic) NSNumber *RSSI;

//Return Mac address first, or NSUUID if there's no Mac address.
- (NSString *)displayAddress;
//Peripheral display name.
- (NSString *)displayName;
//Peripheral actual name.
- (NSString *)peripheralName;

//Reset battery and firmware version information.
- (void)deviceInfoReset;
//Parse the broadcast package dictionary to get the Mac address.
- (void)parseAdvertisementData:(NSDictionary *)dict;
//Parse data to get the Mac address.
- (void)parseMacAddressData:(NSData *)data;
//Set battery information.
- (void)setBatteryWithValue:(NSInteger)value chargeFlag:(BOOL)isCharging;

//Current battery value level.
- (MCBLEDeviceBatteryLevel)batteryLevel;
//Check the battery is charged or not.
- (BOOL)batteryIsCharged;

+ (instancetype)infoWithPeripheral:(CBPeripheral *)peripheral;
+ (instancetype)infoWithDescription:(NSString *)description;

@end
