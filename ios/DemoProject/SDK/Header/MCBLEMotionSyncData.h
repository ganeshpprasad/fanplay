//
//  MCBLEMotionSyncData.h
//  MCBLESDK
//
//  Created by Nick Yang on 2018/6/27.
//  Copyright © 2018 mCube. All rights reserved.
//
//  Combine base time, record, current state data into one type of data for easy management and storage.
//  Used when data is synchronized, it is also used when real-time data is updated.
//

#import <Foundation/Foundation.h>
#import "MCBLEPublicEnum.h"

typedef NS_ENUM(NSInteger, MCBLEMotionSyncDataType)
{
    //Motion base time.
    MCBLEMotionSyncDataTypeBaseTime,
    //Motion record.
    MCBLEMotionSyncDataTypeRecord,
    //Motion current state.
    MCBLEMotionSyncDataTypeCurrentState,
};

@interface MCBLEMotionSyncData : NSObject

@property (assign, nonatomic, readonly) MCBLEMotionSyncDataType type;
@property (strong, nonatomic) NSDate *date;
@property (assign, nonatomic) MCBLEMotionState state;
@property (assign, nonatomic) NSInteger step;
@property (assign, nonatomic) NSInteger minutes;
- (NSString *)description;
- (NSString *)motionStateString;

+ (instancetype)baseTimeDataWithDate:(NSDate *)date;
+ (instancetype)recordDataWithState:(MCBLEMotionState)state step:(NSInteger)step minutes:(NSInteger)minutes;
+ (instancetype)currentStateDataWithDate:(NSDate *)date state:(MCBLEMotionState)state step:(NSInteger)step minutes:(NSInteger)minutes;
+ (NSString *)motionStateString:(MCBLEMotionState)state;

@end
