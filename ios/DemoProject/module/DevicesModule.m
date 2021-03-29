//
//  DeviceModule.m
//  DemoProject
//
//  Created by Sysfore on 28/March/2021.
//

#import <Foundation/Foundation.h>
#import <Foundation/Foundation.h> 
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface RCT_EXTERN_MODULE(DevicesModule, RCTEventEmitter)
//INIT
RCT_EXTERN_METHOD(init)

//Scan Device
RCT_EXTERN_METHOD(scan)
RCT_EXTERN_METHOD(stopScan)

//Device Connection
RCT_EXTERN_METHOD(isConnected:(RCTResponseSenderBlock *) callback)
RCT_EXTERN_METHOD(connect:(NSString *)deviceid callback: (RCTResponseSenderBlock *))
RCT_EXTERN_METHOD(disconnect:(RCTResponseSenderBlock *) callback)

//Heart rate
RCT_EXTERN_METHOD(startHeartRate)
RCT_EXTERN_METHOD(stopHearRate)

//Steps Rate
RCT_EXTERN_METHOD(startSteps)
RCT_EXTERN_METHOD(stopSteps)

//Calorie Rate
RCT_EXTERN_METHOD(startCalorie)
RCT_EXTERN_METHOD(stopCalorie)

//Distance Rate
RCT_EXTERN_METHOD(startDistance)
RCT_EXTERN_METHOD(stopDistance)

//MARK:- Tap / Vistle / Shake
RCT_EXTERN_METHOD(tapIncrementByOne)
RCT_EXTERN_METHOD(vistleIncrementByOne)
RCT_EXTERN_METHOD(shakeIncrementByOne) 
@end

