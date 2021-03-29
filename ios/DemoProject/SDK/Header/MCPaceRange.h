//
//  MCPaceRange.h
//  MCBLESDK
//
//  Created by Nick Yang on 2018/5/2.
//  Copyright © 2018 mCube. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface MCPaceRange : NSObject

@property (assign, nonatomic, readonly) NSInteger start;
@property (assign, nonatomic, readonly) NSInteger end;
@property (assign, nonatomic, readonly) NSInteger pace;
@property (assign, nonatomic, readonly) NSUInteger count;
- (BOOL)isDescending;
- (NSArray <NSString *> *)allValueStrings;
- (NSInteger)valueAtIndex:(NSInteger)index;
- (NSInteger)indexOfValue:(NSInteger)value;
- (NSInteger)fixedValue:(NSInteger)value;

+ (instancetype)rangeWithStart:(NSInteger)start end:(NSInteger)end pace:(NSInteger)pace;
+ (instancetype)rangeWithStart:(NSInteger)start pace:(NSInteger)pace count:(NSUInteger)count;
+ (instancetype)rangeWithRange:(NSRange)range;

+ (instancetype)userAgeRange;
+ (instancetype)userHeightRange;
+ (instancetype)userWeightRange;
+ (instancetype)userStepTargetRange;
+ (instancetype)userSleepTargetRange;
+ (instancetype)userSedentaryIntervalRange;

@end
