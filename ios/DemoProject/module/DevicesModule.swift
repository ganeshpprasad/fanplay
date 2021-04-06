//
//  DeviceModule.swift
//  DemoProject
//
//  Created by Sysfore on 28/March/2021.
//

import UIKit
import AVFoundation
import React 

@objc(DevicesModule)
public class DevicesModule: RCTEventEmitter {
  
  private var hasListeners:Bool = false
  private var devices:[MCBLEDeviceInfo] = []
  private var callback:(RCTResponseSenderBlock)? = nil
  
  @objc override init() {
    super.init()
    MCBLESDK.setConnectDelegate(self)
  }
  
  public override class func requiresMainQueueSetup() -> Bool {
    return true
  }
  
  public override func startObserving() {
    hasListeners = true
  }
  
  public override func stopObserving() {
    hasListeners = false
  }
  
  public override func supportedEvents() -> [String]! {
    return ["devicesScanEvent"]
  }
}

//MARK:- Status Checking
extension DevicesModule {
  
  //check the bluetooth status
  private func checkBluetoothStatus()-> MCManagerState {
    return MCBLESDK.managerState()
  }
  
  //check if blue tooth is on
  private func isBluetoothOn()->Bool {
    return MCBLESDK.managerStateIsPoweredOn()
  }
  
  //is device connecyed
  private func isDeviceConnected()->Bool {
    return MCBLESDK.connectionStateIsConnected()
  }
  
  //is device connecyed
  private func deviceConnectionState()->MCBLEConnectionState {
    return MCBLESDK.connectionState()
  }
}

//MARK:- Start scan and stop scan
extension DevicesModule {
  
  //start the device search
  @objc func scan() {
    print("scanDevices")
    MCBLESDK.scan(15)
  }
  
  //stop the device search
  @objc func stopScan() {
    print("stopScan")
    MCBLESDK.stopScan()
  }
} 

//MARK:- Establish Connection Class
extension DevicesModule {
  
  @objc func isConnected(_ callback: @escaping (RCTResponseSenderBlock)){
    print("isConnected")
    self.callback!([["flag":isDeviceConnected()]])
  }
  
  //connect to the selected device
  @objc func connect(_ deviceid:String, callback: @escaping (RCTResponseSenderBlock)){
    print("startConnectToDevice")
    self.stopScan()
    
    self.callback = callback
    
    let deviceInfor = self.devices.first { (deviceInfo) -> Bool in
      return deviceInfo.displayAddress() == deviceid
    }
    
    if let deviceInfor = deviceInfor {
      print("Connecting")
      MCBLESDK.connect(deviceInfor)
    }else{
      self.callback!([["flag":false]])
    }
  }
  
  @objc func disconnect(_ callback: @escaping (RCTResponseSenderBlock)){
    print("disConnectToDevice")
    MCBLESDK.disconnect()
    callback([["flag":true]])
  }
}



//MARK:- Connect Delegate
extension  DevicesModule : MCBLESDKConnectDelegate {
  
  //bluetooth manager status update
  public func onManagerStateUpdate(_ managerState: MCManagerState) {
    print("onManagerStateUpdate")
  }
  
  //on scan ended
  public func onScanEnd() {
    print("onScanEnd")
  }
  
  //on device discover
  public func onDiscoverDevice(_ device: MCBLEDeviceInfo!) {
    print("device:\(String(describing: device))")
    if !self.devices.contains(device){
      self.devices.append(device)
    }
    self.sendEvent(withName: "devicesScanEvent", body:  ["address":device.displayAddress()])
  }
  
  //on device conection failed
  public func onDeviceConnectFailed(_ device: MCBLEDeviceInfo!) {
    print("onDeviceConnectFailed")
    self.callback!([["flag":false]])
  }
  
  //on device disconnected
  public func onDeviceDisconnect(_ device: MCBLEDeviceInfo!) {
    print("onDeviceDisconnect")
    self.callback!([["flag":false]])
  }
  
  //on device connected status
  public func onDeviceConnectSucceeded(_ device: MCBLEDeviceInfo!) {
    print("onDeviceConnectSucceeded")
    self.callback!([["flag":true]])
  }
}

//MARK:- Tap / Vistle / Shake
extension  DevicesModule {
  
  @objc func insertBand(_ address: String, type:Int) {
    var deviceModel = DeviceModel.init()
    deviceModel.address = address
    deviceModel.type = type
    deviceModel.sId = BTUserDefaults.shared.getString(key: .SID)
    print(try! DeviceHelper.insert(item: deviceModel))
  }
}
