//
//  MCBLEFirmwareVersion.h
//  MCBLESDK
//
//  Created by Nick Yang on 2018/4/27.
//  Copyright © 2018 mCube. All rights reserved.
//
//  Firmware version
//  Format: AB.CD.EF.GH
//  AB: Main version.
//  CD: Subversion.
//  EF: Reserved, for debugging.
//  GH: Hardware feature.
//
//  Sample: 31.2d.00.01 (lowercase string)
//  Separator: '.'.
//

#import <Foundation/Foundation.h>

@interface MCBLEFirmwareVersion : NSObject <NSCopying>

@property (assign, nonatomic, readonly) NSUInteger version;

- (NSUInteger)mainVersion;
- (NSUInteger)subversion;
- (NSUInteger)reservedSegment;
- (NSUInteger)hardwareFeature;

/*
 Firmware serial version value.
 Obtained by ignoring the firmware version CD & EF segments.
 Firmware version           :31.2d.77.01
 Firmware serial version    :31.00.00.01
 */
- (NSUInteger)serialVersion;

//Firmware version string, sample: 31.2d.00.01
- (NSString *)versionString;
//Firmware serial version string
- (NSString *)versionSerialString;

- (NSString *)description;

//Firmware version equal comparison
- (BOOL)isEqual:(MCBLEFirmwareVersion *)object;
//Firmware serial version equal comparison
- (BOOL)isSerialEqual:(MCBLEFirmwareVersion *)object;
//Firmware version comparison
- (BOOL)updatedCompareGreaterThan:(MCBLEFirmwareVersion *)object;
- (BOOL)updatedCompareLessThan:(MCBLEFirmwareVersion *)object;

+ (instancetype)versionWithVersionValue:(NSUInteger)value;
+ (instancetype)versionWithVersionString:(NSString *)valueString;
+ (instancetype)versionWithBinFilePath:(NSString *)file;

@end
