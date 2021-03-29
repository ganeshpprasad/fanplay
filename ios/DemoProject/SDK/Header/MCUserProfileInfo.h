//
//  MCUserProfileInfo.h
//  MCBLESDK
//
//  Created by Nick Yang on 2017/8/24.
//  Copyright © 2017 mCube. All rights reserved.
//

@import Foundation;

extern NSInteger const MCUserHeightValueDefault;
extern NSInteger const MCUserWeightValueDefault;

typedef NS_ENUM(NSInteger, MCUserGenderType)
{
    MCUserGenderTypeMale,
    MCUserGenderTypeFemale,
    MCUserGenderTypeDefault = MCUserGenderTypeMale,
};

typedef NS_ENUM(NSInteger, MCUserWearingHabit)
{
    MCUserWearingHabitRightHand,
    MCUserWearingHabitLeftHand,
    MCUserWearingHabitDefault   = MCUserWearingHabitLeftHand,
};

typedef NS_ENUM(NSInteger, MCDistanceUnit)
{
    MCDistanceUnitKilometer,
    MCDistanceUnitMile,
    MCDistanceUnitDefault   = MCDistanceUnitKilometer,
};

@interface MCUserProfileInfo : NSObject <NSCopying>

@property (strong, nonatomic) NSDate *birthday;
@property (assign, nonatomic) MCUserGenderType gender;
@property (assign, nonatomic) NSInteger height;
@property (assign, nonatomic) NSInteger weight;
@property (assign, nonatomic) MCUserWearingHabit wearingHabit;
@property (assign, nonatomic) MCDistanceUnit distanceUnit;
@property (assign, nonatomic) NSInteger stepTarget;

- (NSInteger)userAge;
- (BOOL)isMale;
- (BOOL)isRightHandHabit;
- (BOOL)isDistanceUnitKilometer;

//@[Beginning,Terminal]
+ (NSArray <NSDate *>*)birthdayRangeDates;

@end
