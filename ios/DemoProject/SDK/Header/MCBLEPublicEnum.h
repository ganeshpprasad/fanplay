//
//  MCBLEPublicEnum.h
//  MCBLESDK
//
//  Created by Nick Yang on 2017/10/13.
//  Copyright © 2017 mCube. All rights reserved.
//

#ifndef MCBLEPublicEnum_h
#define MCBLEPublicEnum_h

/*
 Enum MCBLEConnectionState:
 Device connection state.
 
 It's different from CBPeripheralState because each state function is defined differently.
 */
typedef NS_ENUM(NSInteger, MCBLEConnectionState)
{
    MCBLEConnectionStateDisconnected,
    MCBLEConnectionStateConnecting,
    MCBLEConnectionStateConnected,
};

/*
 Enum MCBLEDeviceFeature:
 Device feature item.
 */
typedef NS_OPTIONS(NSUInteger, MCBLEDeviceFeature)
{
    //Pedometer
    MCBLEDeviceFeaturePedometer     = 1 << 0, //0000 0001
    //Sleep
    MCBLEDeviceFeatureSleep         = 1 << 1, //0000 0010
    //Heart Rate
    MCBLEDeviceFeatureHeartRate     = 1 << 2, //0000 0100
    //Blood Pressure
    MCBLEDeviceFeatureBloodPressure = 1 << 3, //0000 1000
};

/*
 Enum MCBLEDeviceFeatureMode:
 Device supported features.
 */
typedef NS_ENUM(NSUInteger, MCBLEDeviceFeatureMode)
{
    //Support pedometer, sleep
    MCBLEDeviceFeatureMode0     = MCBLEDeviceFeaturePedometer | MCBLEDeviceFeatureSleep,
    
    //Support pedometer, sleep, heart rate
    MCBLEDeviceFeatureMode1     = MCBLEDeviceFeatureMode0 | MCBLEDeviceFeatureHeartRate,
    
    //Support pedometer, sleep, heart rate, blood pressure
    MCBLEDeviceFeatureMode2     = MCBLEDeviceFeatureMode1 | MCBLEDeviceFeatureBloodPressure,
    
    MCBLEDeviceFeatureModeMax   = MCBLEDeviceFeatureMode2,
};

/*
 Enum MCManagerState:
 Bluetooth state.
 
 Equivalent to CBManagerState, CBCentralManagerState and CBPeripheralManagerState.
 The purpose is to solve the compatibility problem of CBCentralManager.state type in different versions of iOS.
 */
typedef NS_ENUM(NSInteger, MCManagerState)
{
    MCManagerStateUnknown   = 0,//CBManagerStateUnknown
    MCManagerStateResetting,    //CBManagerStateResetting
    MCManagerStateUnsupported,  //CBManagerStateUnsupported
    MCManagerStateUnauthorized, //CBManagerStateUnauthorized
    MCManagerStatePoweredOff,   //CBManagerStatePoweredOff
    MCManagerStatePoweredOn,    //CBManagerStatePoweredOn
};

/*
 Enum MCBLEMotionState:
 Motion state.
 */
typedef NS_ENUM(NSInteger, MCBLEMotionState)
{
    MCBLEMotionStateStationary  = 0,//0
    MCBLEMotionStateWalking,        //1
    MCBLEMotionStateRunning,        //2
    MCBLEMotionStateSleep,          //3
    MCBLEMotionStateAwake,          //4
    MCBLEMotionStateRestless,       //5
    //Others, depend on algorithm
};

#endif /* MCBLEPublicEnum_h */
