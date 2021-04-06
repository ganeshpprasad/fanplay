//
//  LoginBridge.m
//  DemoProject
//
//  Created by Sysfore on 29/March/2021.
//

#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface RCT_EXTERN_MODULE(LoginModule, RCTEventEmitter)
//INIT
RCT_EXTERN_METHOD(init)

//Insert Or Update
RCT_EXTERN_METHOD(insertOrUpdate:(NSString *)tokenId tokenExpires:(NSInteger *)tokenExpires displayName:(NSString *)displayName type:(NSInteger *)type callback:(RCTResponseSenderBlock *)callback)
RCT_EXTERN_METHOD(insertTeam:(NSInteger *)teamIdServer callback:(RCTResponseSenderBlock *)callback)
@end
