//
//  MCBLEDisplaySettingInfo.h
//  MCBLESDK
//
//  Created by Nick Yang on 2017/8/26.
//  Copyright © 2017 mCube. All rights reserved.
//
//  Device display setting information
//

#import <Foundation/Foundation.h>

typedef NS_ENUM(NSInteger, MCBLETimeFormatType)
{
    MCBLETimeFormatTypeTimeOnly     = 0,
    MCBLETimeFormatTypeDateAndTime  = 1,
    MCBLETimeFormatTypeDefault      = MCBLETimeFormatTypeTimeOnly,
};

@interface MCBLEDisplaySettingInfo : NSObject <NSCopying>

//Time format.
@property (assign, nonatomic) MCBLETimeFormatType timeFormat;
//Raise the wrist to light up the screen.
@property (assign, nonatomic) BOOL raiseHandDisplayEnable;

- (BOOL)isTimeOnlyTypeTimeFormat;

@end
