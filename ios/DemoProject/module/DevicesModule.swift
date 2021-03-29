//
//  DeviceModule.swift
//  DemoProject
//
//  Created by Sysfore on 28/March/2021.
//

import React
import HealthKit

@objc(DevicesModule)
public class DevicesModule: RCTEventEmitter {
  
  private var hasListeners:Bool = false
  private var devices:[MCBLEDeviceInfo] = []
  private var callback:(RCTResponseSenderBlock)? = nil
  
  private var heartRateTime: RepeatSubscriberId?
  private var stepsTime: RepeatSubscriberId?
  private var calorieTime: RepeatSubscriberId?
  private var distanceTime: RepeatSubscriberId?
  
  @objc override init() {
    super.init()
    MCBLESDK.setAutoSyncDataWhenConnected(true)
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
    return ["devicesScanEvent", "devicesHeartRateScanEvent", "deviceStepsCalorieDistanceDiscover"]
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
  
  @objc func isConnected(callback: @escaping (RCTResponseSenderBlock)){
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


//MARK:- Monitor Hear rate class
extension DevicesModule {
  //start heart rate
  @objc func startHeartRate() {
    print("startHeartRate")
    self.addObserver()
    MCBLESDK.setHeartRateMeasure(true)
  }
  
  //stop heart rate
  @objc func stopHearRate() {
    print("stopHearRate")
    self.removeObserver()
    MCBLESDK.setHeartRateMeasure(false)
  }
  
  //add observer for heart rate
  private func addObserver(){
    NotificationCenter.default.addObserver(self, selector: #selector(heartRateObserver(_:)), name: NSNotification.Name(rawValue: MCBLENotificationDeviceHeartRateMeasureComplete), object: nil)
  }
  
  //add observer for heart rate
  private func removeObserver(){
    NotificationCenter.default.removeObserver(self, name: NSNotification.Name(rawValue: MCBLENotificationDeviceHeartRateMeasureComplete), object: nil)
  }
  
  //return the heart rate captured
  @objc func heartRateObserver(_ sender: NSNotification){
    let data = sender.object as! MCBLEOfflineSyncData
    print(data)
    self.sendEvent(withName: "devicesHeartRateScanEvent", body:  [
      "hr": data.heartRate,
      "step" : 0,
      "cal" : 0,
      "dist":0
    ])
  }
}


//MARK:- Steps / Calorie/ Distance
extension DevicesModule {
  @objc func startSteps() {
    print("startSteps")
    addStepCalorieDistanceObserver()
    MCBLESDK.readRealTimeValue()
  }
  
  @objc func stopSteps() {
    print("stopSteps")
    removeStepCalorieDistanceObserver()
  }
  
  //add observer for heart rate
  private func addStepCalorieDistanceObserver(){
    NotificationCenter.default.addObserver(self, selector: #selector(realTimeDataDidUpdate(_:)), name: NSNotification.Name(rawValue: MCBLENotificationDeviceHeartRateMeasureComplete), object: nil)
  }
  
  //add observer for heart rate
  private func removeStepCalorieDistanceObserver(){
    NotificationCenter.default.removeObserver(self, name: NSNotification.Name(rawValue: MCBLENotificationDeviceHeartRateMeasureComplete), object: nil)
  }
  
  @objc private func realTimeDataDidUpdate(_ sender: NSNotification){
    var distanceUnit = ""
    var distanceString = ""
    var calorieString = ""
    var stepCount = 0
    
    if let data = sender.object as? MCBLEMotionSyncData {
      stepCount = data.step
      distanceUnit = MCBLESDK.distanceUnitIsKM() ? "km" : "mi"
      distanceString = MCBLESDK.distanceString(withStep: UInt(stepCount))
      calorieString = MCBLESDK.calorieString(withStep: UInt(stepCount))
    }
    removeStepCalorieDistanceObserver()
    self.sendEvent(withName: "deviceStepsCalorieDistanceDiscover", body:  [
      "hr": 0,
      "step" : stepCount,
      "cal" : calorieString,
      "dist" : distanceString,
      "distUnit" : distanceUnit
    ])
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
  
  @objc func tapIncrementByOne() {
    
  }
  
  @objc func vistleIncrementByOne() {
    
  }
  
  @objc func shakeIncrementByOne() {
    
  }
}
