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
RCT_EXTERN_METHOD(connect:(NSString *)deviceid callback:(RCTResponseSenderBlock *)callback)
RCT_EXTERN_METHOD(disconnect:(RCTResponseSenderBlock *) callback)

//insert band
RCT_EXTERN_METHOD(insertBand:(NSString *) address type:(NSInteger *)type)
@end

