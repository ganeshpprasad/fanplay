//
//  HealthKitModuleBridge.m
//  DemoProject
//
//  Created by Sysfore on 26/March/2021.
//

#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface RCT_EXTERN_MODULE(HealthKitModule, RCTEventEmitter)
//INIT
RCT_EXTERN_METHOD(init)

//Check if health kit is available
RCT_EXTERN_METHOD(isHealthAvailable:(RCTResponseSenderBlock *) callback)

//check for autorization
RCT_EXTERN_METHOD(requestAuthorization:(RCTResponseSenderBlock *) callback)

//get the heart beat query 
RCT_EXTERN_METHOD(startFanEngageHeart)

//stop the heart beat query
RCT_EXTERN_METHOD(stopFanEngageHeart)

//get the step query
RCT_EXTERN_METHOD(startFanEngageSteps)

//stop the step query 
RCT_EXTERN_METHOD(stopFanEngageSteps)
@end
