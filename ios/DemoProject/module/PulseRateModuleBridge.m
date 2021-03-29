//
//  PulseRateModuleBridge.m
//  DemoProject
//
//  Created by Sysfore on 26/March/2021.
//

#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface RCT_EXTERN_MODULE(PulseRateModule, RCTEventEmitter)
//INIT
RCT_EXTERN_METHOD(init)

//Check if health kit is available
RCT_EXTERN_METHOD(getPulseRate)
@end
