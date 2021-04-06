//
//  FanEngageBridge.m
//  DemoProject
//
//  Created by Sysfore on 29/March/2021.
//

#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface RCT_EXTERN_MODULE(FanEngageModule, RCTEventEmitter)
//INIT
RCT_EXTERN_METHOD(init)

//Sync the device 
RCT_EXTERN_METHOD(sync:(NSInteger *)type callback:(RCTResponseSenderBlock *)callback)

//Heart rate
RCT_EXTERN_METHOD(startFanEngageHeartRate:(NSInteger *)type)
RCT_EXTERN_METHOD(stopFanEngageHeartRate)

//Steps Rate
RCT_EXTERN_METHOD(startFanEngageSteps)
RCT_EXTERN_METHOD(stopFanEngageSteps) 

//MARK:- Tap / Vistle / Shake
RCT_EXTERN_METHOD(updateHeartRate:(NSInteger *)heartRate type:(NSInteger *)type)
RCT_EXTERN_METHOD(updateTapCount:(NSInteger *)count playerId:(NSInteger *)playerId)
RCT_EXTERN_METHOD(updateWaveCount:(NSInteger *)count playerId:(NSInteger *)playerId)
RCT_EXTERN_METHOD(incrementWhistle:(NSInteger *)count playerId:(NSInteger *)playerId)
RCT_EXTERN_METHOD(validateSignIn:(RCTResponseSenderBlock *) callback)
RCT_EXTERN_METHOD(getFanEmote:(NSString *)teamId callback:(RCTResponseSenderBlock *)callback)
RCT_EXTERN_METHOD(getFEDetailsByTeamId:(NSString *)teamId callback:(RCTResponseSenderBlock *)callback)

//Pedo Meter
RCT_EXTERN_METHOD(callPedoMeter)

// get fan engagement data
RCT_EXTERN_METHOD(getFanEngageData:(RCTResponseSenderBlock *) callback)
@end
