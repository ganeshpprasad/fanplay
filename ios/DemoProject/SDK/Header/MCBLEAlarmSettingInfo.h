//
//  MCBLEAlarmSettingInfo.h
//  MCBLESDK
//
//  Created by Nick Yang on 2017/8/28.
//  Copyright © 2017 mCube. All rights reserved.
//

@import Foundation;

typedef NS_ENUM(NSUInteger, MCBLEAlarmRepeatDay)
{
    MCBLEAlarmRepeatNone        = 0x00,
    
    MCBLEAlarmRepeatSunday      = 0x01 << 0,
    MCBLEAlarmRepeatMonday      = 0x01 << 1,
    MCBLEAlarmRepeatTuesday     = 0x01 << 2,
    MCBLEAlarmRepeatWednesday   = 0x01 << 3,
    MCBLEAlarmRepeatThursday    = 0x01 << 4,
    MCBLEAlarmRepeatFriday      = 0x01 << 5,
    MCBLEAlarmRepeatSaturday    = 0x01 << 6,
    
    MCBLEAlarmRepeatWeekdays    =
    MCBLEAlarmRepeatMonday      |
    MCBLEAlarmRepeatTuesday     |
    MCBLEAlarmRepeatWednesday   |
    MCBLEAlarmRepeatThursday    |
    MCBLEAlarmRepeatFriday,
    
    MCBLEAlarmRepeatWeekend     =
    MCBLEAlarmRepeatSaturday    |
    MCBLEAlarmRepeatSunday,
    
    MCBLEAlarmRepeatEveryday    =
    MCBLEAlarmRepeatWeekdays    |
    MCBLEAlarmRepeatWeekend,
    
    MCBLEAlarmRepeatDefault     =
    MCBLEAlarmRepeatNone,
};

@interface MCBLEAlarmInfo : NSObject <NSCopying>

@property (assign, nonatomic) BOOL openFlag;
@property (assign, nonatomic) NSInteger timeHour;
@property (assign, nonatomic) NSInteger timeMinute;
@property (assign, nonatomic) MCBLEAlarmRepeatDay repeatDays;

/*
 When it's once type alarm (repeatDays == MCBLEAlarmRepeatNone), Property 'fireDate' is valid.
 */
@property (strong, nonatomic) NSDate *fireDate;

- (BOOL)isEqual:(MCBLEAlarmInfo *)object;
- (NSString *)timeString;
- (NSData *)settingInfoData;
@end

@interface MCBLEAlarmSettingInfo : NSObject <NSCopying>

@property (strong, nonatomic) MCBLEAlarmInfo *alarm1st;
@property (strong, nonatomic) MCBLEAlarmInfo *alarm2nd;
@property (strong, nonatomic) MCBLEAlarmInfo *alarm3rd;

- (NSArray <MCBLEAlarmInfo *> *)alarmList;
- (NSData *)settingInfoData;
- (BOOL)isAllAlarmsClosed;
- (BOOL)isEqual:(MCBLEAlarmSettingInfo *)object;

//Check once type alarm status
- (void)refreshSettingInfo;

@end
