//
//  FanEngageModule.swift
//  DemoProject
//
//  Created by Sysfore on 29/March/2021.
//

import UIKit
import AVFoundation
import React

@objc(FanEngageModule)
class FanEngageModule: RCTEventEmitter {
  
  private let homeRepo = HomeRepo.init()
  
  private var heartRateTime: Timer?
  private var stepsRateTime: Timer?
  
  @objc override init() {
    super.init()
    DispatchQueue.main.async {
      MCBLESDK.setAutoSyncDataWhenConnected(true)
    }
  }
  
  public override class func requiresMainQueueSetup() -> Bool {
    return true
  }
  
  var hasListeners = false
  
  public override func startObserving() {
    hasListeners = true
  }
  
  public override func stopObserving() {
    hasListeners = false
  }
  
  public override func supportedEvents() -> [String]! {
    return ["devicesHeartRateScanEvent", "deviceStepsCalorieDistanceDiscover"]
  }
} 

//MARK:- Monitor Hear rate class
extension FanEngageModule : PulseViewDelegate {
  
  @objc func sync(_ type: Int, callback:RCTResponseSenderBlock) {
    //1 – camera, 2 – health kit , 3 - Fanband
    if type == 3 {
      callback([MCBLESDK.connectionStateIsConnected()])
    }
  }
  
  //start heart rate
  @objc func startFanEngageHeartRate(_ type: Int) {
    //Camera
    if type == 1 {
      self.callCameraToGetHeart()
    }
    
    //Health Kit
    if type == 2 {
      
    }
    
    //Fan Band
    if type == 3 {
      self.callFanBandToGetHeart()
    }
  }
  
  //stop heart rate
  @objc func stopFanEngageHeartRate() {
    print("stopHearRate")
    self.removeObserver()
    MCBLESDK.setHeartRateMeasure(false)
    if heartRateTime != nil {
      heartRateTime?.invalidate()
      heartRateTime = nil
    }
  }
  
  //return the heart rate captured
  @objc func heartRateObserver(_ sender: NSNotification){
    if let data = sender.object as? MCBLEOfflineSyncData {
      print(data)
      self.updateHeartRate(data.heartRate, type: 3)
      self.sendCustomEvent(heartRate: [ "heartRate":data.heartRate,"status": false,"error": "Captured Device Heart Rate"])
    }else{
      self.sendCustomEvent(heartRate: [ "heartRate":0,"status": true,"error": "Did not capture heart rate"])
    }
  }
  
  //add observer for heart rate
  private func addObserver(){
    NotificationCenter.default.addObserver(self, selector: #selector(heartRateObserver(_:)), name: NSNotification.Name(rawValue: MCBLENotificationDeviceHeartRateMeasureComplete), object: nil)
  }
  
  //add observer for heart rate
  private func removeObserver(){
    NotificationCenter.default.removeObserver(self, name: NSNotification.Name(rawValue: MCBLENotificationDeviceHeartRateMeasureComplete), object: nil)
  }
  
  func onHeartRateReceved(_ heartRate: Any) {
    if let dict = heartRate as? [AnyHashable: Any] {
      if let hearRate = dict["heartRate"] as? Int , hearRate > 0{
        self.updateHeartRate(hearRate, type: 1)
      }
    }
    sendCustomEvent(heartRate: heartRate)
  }
  
  private func sendCustomEvent(heartRate:Any){
    self.sendEvent(withName: "devicesHeartRateScanEvent", body:  heartRate)
  }
  
  private func callCameraToGetHeart(){
    DispatchQueue.main.async {
      if let appDelegate = UIApplication.shared.delegate as? AppDelegate {
        appDelegate.goToPulseViewController(pulseViewDelegate: self)
      }else{
        print("App Delegate not found ")
        self.sendCustomEvent(heartRate: [ "heartRate":0,"status": true, "error": "Failed to capture the heart rate"])
      }
    }
  }
  
  private func callFanBandToGetHeart(){
    print("startHeartRate")
    MCBLESDK.setHeartRateMeasure(false)
    self.removeObserver()
    self.addObserver()
    MCBLESDK.setHeartRateMeasure(true)
    DispatchQueue.main.async {
      self.heartRateTime = Timer.scheduledTimer(withTimeInterval: 30, repeats: true) { (Timer) in
        print("Starting 30 sec heart timer")
        MCBLESDK.setHeartRateMeasure(false)
        self.removeObserver()
        self.addObserver()
        MCBLESDK.setHeartRateMeasure(true)
      }
    }
  }
  
  @objc func updateHeartRate(_ heartRate: Int, type: Int) {
    var hr = HeartRateModel.init()
    hr.heartRate = heartRate
    hr.type = type
    hr.sId = BTUserDefaults.shared.getString(key: .SID)
    print(try! HeartRateHelper.insert(item: hr))
//    if let appDelegate = UIApplication.shared.delegate as? AppDelegate {
//      //Calling Api Create Heart Rate
//      HomeRepo().callCreateFanEngageMentApi(heartRateModel: hr)
//    }
    HomeRepo().callCreateFanEngageMentApi(heartRateModel: hr)
  }
} 

//MARK:- Steps / Calorie/ Distance
extension FanEngageModule {
  
  @objc func startFanEngageSteps() {
    print("startSteps")
    self.removeStepCalorieDistanceObserver()
    self.addStepCalorieDistanceObserver()
    MCBLESDK.readRealTimeValue()
    DispatchQueue.main.async {
      self.stepsRateTime = Timer.scheduledTimer(withTimeInterval: 30, repeats: true) { (Timer) in
        print("Starting 30 sec steps timer")
        self.removeStepCalorieDistanceObserver()
        self.addStepCalorieDistanceObserver()
        MCBLESDK.readRealTimeValue()
      }
    }
  }
  
  @objc func stopFanEngageSteps() {
    print("stopSteps")
    self.removeStepCalorieDistanceObserver()
    if self.stepsRateTime != nil {
      self.stepsRateTime?.invalidate()
      self.stepsRateTime = nil
    }
  }
  
  //add observer for heart rate
  private func addStepCalorieDistanceObserver(){
    NotificationCenter.default.addObserver(self, selector: #selector(realTimeDataDidUpdate(_:)), name: NSNotification.Name(rawValue: MCBLENotificationDeviceRealTimeDataDidUpdate), object: nil)
  }
  
  //add observer for heart rate
  private func removeStepCalorieDistanceObserver(){
    NotificationCenter.default.removeObserver(self, name: NSNotification.Name(rawValue: MCBLENotificationDeviceRealTimeDataDidUpdate), object: nil)
  }
  
  @objc private func realTimeDataDidUpdate(_ sender: NSNotification){
    print("realTimeDataDidUpdate")
    var distanceUnit = ""
    var distanceString = ""
    var calorieString = ""
    var stepCount = 0
    
    if let data = sender.object as? MCBLEMotionSyncData {
      stepCount = data.step
      distanceUnit = MCBLESDK.distanceUnitIsKM() ? "km" : "mi"
      distanceString = MCBLESDK.distanceString(withStep: UInt(stepCount))
      calorieString = MCBLESDK.calorieString(withStep: UInt(stepCount))
      self.sendEvent(withName: "deviceStepsCalorieDistanceDiscover", body:  [
        "stepCount" : stepCount,
        "calories" : calorieString,
        "distance" : distanceString,
        "distanceUnit" : distanceUnit,
        "status": false,
        "error": "Fetched Step Rate"
      ])
    }else{
      self.sendEvent(withName: "deviceStepsCalorieDistanceDiscover", body:  [
        "stepCount" : 0,
        "calories" : "",
        "distance" : "",
        "distanceUnit" : "",
        "status": true,
        "error": "Could not fetch data"
      ])
    }
  }
}

//MARK:- Tap Count
extension FanEngageModule {
  
  @objc func updateTapCount(_ count: Int, playerId: Int) {
    self.homeRepo.updateTapCount(count, playerId: playerId)
  }
  
  @objc func updateWaveCount(_ count: Int, playerId: Int) {
    self.homeRepo.updateWaveCount(count, playerId: playerId)
  } 
  
  @objc func incrementWhistle(_ count: Int, playerId: Int) {
    self.homeRepo.incrementWhistle(count, playerId: playerId)
  }
  
  @objc func validateSignIn(_ callback: @escaping (RCTResponseSenderBlock)){
    self.homeRepo.validateSignIn(callback)
  }
  
  @objc func getFanEmote(_ teamId:String,callback: @escaping (RCTResponseSenderBlock)){
    self.homeRepo.getFanEmote(teamId, callback: callback)
  }
  
  @objc func getFEDetailsByTeamId(_ teamId:String,callback: @escaping (RCTResponseSenderBlock)){
    self.homeRepo.getFEDetailsByTeamId(teamId, callback: callback)
  }
  
  @objc func getFanEngageData(_ callback: @escaping (RCTResponseSenderBlock)){
    let userId = BTUserDefaults.shared.getString(key: .SID)
    let teamId = BTUserDefaults.shared.getInt(key: .TEAM_ID)
    let data = [
      ["totalTapCount": FanDataHelper.getCurrentCount(userId: userId, teamId: teamId)],
      ["totalWaveCount": WaveDatahelper.getCurrentCount(userId: userId, teamId: teamId)],
      ["totalWhistleEarned": WhistleDataHelper.getCurrentCount(userId: userId, teamId: teamId, position: 1)],
      ["totalWhistleRedeemed": WhistleDataHelper.getCurrentCount(userId: userId, teamId: teamId, position: 2)]
    ]
    callback(data)
  }
}

//get pedo meter
extension FanEngageModule {
  @objc func callPedoMeter(){
    DispatchQueue.main.async {
      if let appDelegate = UIApplication.shared.delegate as? AppDelegate {
        appDelegate.goToPedoMeterViewController()
      }else{
        print("App Delegate not found ")
      }
    }
  }
}
