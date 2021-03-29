//
//  MCBLEOfflineSyncData.h
//  MCBLESDK
//
//  Created by Nick Yang on 2018/6/27.
//  Copyright © 2018 mCube. All rights reserved.
//
//  Used to carry offline heart rate blood pressure recording data
//  Used when data is synchronized, and is also used when broadcasting heart rate and blood pressure measured in real time.
//

#import <Foundation/Foundation.h>

typedef NS_ENUM(NSInteger, MCBLEOfflineSyncDataType)
{
    //Base time.
    MCBLEOfflineSyncDataTypeBaseTime,
    //Heart rate.
    MCBLEOfflineSyncDataTypeHeartRate,
    //Blood pressure.
    MCBLEOfflineSyncDataTypeBloodPressure,
    //SpO2
    MCBLEOfflineSyncDataTypeSPO2,
};

@interface MCBLEOfflineSyncData : NSObject

//***Important: "Type" is the single flag to identify data type.
@property (assign, nonatomic, readonly) MCBLEOfflineSyncDataType type;
@property (strong, nonatomic) NSDate *date;
@property (assign, nonatomic) NSInteger heartRate;
@property (assign, nonatomic) NSInteger systolicPressure;
@property (assign, nonatomic) NSInteger diastolicPressure;
@property (assign, nonatomic) NSInteger SPO2;
@property (assign, nonatomic) NSInteger seconds;
- (NSString *)description;

+ (instancetype)baseTimeDataWithDate:(NSDate *)date;
+ (instancetype)heartRateDataWithValue:(NSInteger)heartRate seconds:(NSInteger)seconds;
+ (instancetype)bloodPressureDataWithSystolicPressure:(NSInteger)systolic diastolicPressure:(NSInteger)diastolic seconds:(NSInteger)seconds;
+ (instancetype)SPO2DataWithValue:(NSInteger)SPO2Value seconds:(NSInteger)seconds;

@end
