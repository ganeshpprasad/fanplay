//
//  TeamBridge.m
//  DemoProject
//
//  Created by Sysfore on 06/April/2021.
//

#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface RCT_EXTERN_MODULE(TeamModule, RCTEventEmitter)
//INIT
RCT_EXTERN_METHOD(init)

RCT_EXTERN_METHOD(getDefaultTeam:(RCTResponseSenderBlock *)callback)
RCT_EXTERN_METHOD(getPlayers:(RCTResponseSenderBlock *)callback)
@end
