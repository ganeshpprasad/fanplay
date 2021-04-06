//
//  HealthKitModule.swift
//  DemoProject
//
//  Created by Sysfore on 26/March/2021.
//

import UIKit
import React
import HealthKit

@objc(HealthKitModule)
class HealthKitModule: RCTEventEmitter {
  var heartObserverQuery: HKObserverQuery?
  var stepObserverQuery: HKObserverQuery?
  
  private var heartRateTime: Timer?
  private var stepsRateTime: Timer?
  
  private var timer: Timer?
  
  private let bpmType = HKObjectType.quantityType(forIdentifier: HKQuantityTypeIdentifier.heartRate)
  
  private var healthStore: HKHealthStore = HKHealthStore()
  
  private var callback:(RCTResponseSenderBlock)? = nil
  
  @objc public override init() {
    super.init()
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
    return ["healthKitHeartRateEvent"]
  }
  
  private func sendCustomEvent(body:Any){
    print(body)
    self.sendEvent(withName: "healthKitHeartRateEvent", body: body)
  }
}

//MARK:- Heart rate changed
extension HealthKitModule {
  private func onHeartRateChanged(_ heartRate:Double, status: Bool, error:Error?) {
    var hr = 0.0
    var st = false
    var er = ""
    if status {
      hr = heartRate
      st = false
      er = "Success"
    }else{
      hr = 0
      st = true
      if let err = error {
        er = err.localizedDescription
      }else{
        er = "Failed to fetch heart rate"
      }
    }
    if hr > 0 {
      var rate = HeartRateModel.init()
      rate.heartRate = Int(hr)
      rate.type = 4
      rate.sId = BTUserDefaults.shared.getString(key: .SID)
      print(try! HeartRateHelper.insert(item: rate))
      HomeRepo().callCreateFanEngageMentApi(heartRateModel: rate)
//      if let appDelegate = UIApplication.shared.delegate as? AppDelegate {
//        //Calling Api Create Heart Rate
//        appDelegate.callCreateFanEngageMentApi(heartRateModel: rate)
//      }
    }
    self.sendCustomEvent(body: [ "heartRate": hr, "status": st, "error": er ])
  }
  
  private func onStepsChanged(_ steps:Int, status: Bool, error:Error?) {
    var step = 0  
    var st = false
    var er = ""
    if status {
      step = steps
      st = false
      er = "Success"
    }else{
      step = 0
      st = true
      if let err = error {
        er = err.localizedDescription
      }else{
        er = "Failed to fetch steps rate"
      }
    }
    self.sendCustomEvent(body: [ "stepsRate": step, "status": st, "error": er ])
  }
}

// MARK:- Check if the health kit is available
extension HealthKitModule {
  // Check if health is available on this device
  private func isHealthAvailable() -> Bool {
    return HKHealthStore.isHealthDataAvailable()
  }
  
  // Check if this app is authorized to write the necessary data to Health
  private func isHealthAuthorized() -> Bool {
    return healthStore.authorizationStatus(for: bpmType!) == HKAuthorizationStatus.sharingAuthorized
  }
  
  @objc public func isHealthAvailable(_ callback: @escaping (RCTResponseSenderBlock)){
    callback([["flag":isHealthAvailable()]])
  }
  
  // Request authorization from the user
  @objc public func requestAuthorization(_ callback: @escaping (RCTResponseSenderBlock)){
    let bpmTypes : Set<HKSampleType> = [
      HKObjectType.quantityType(forIdentifier: HKQuantityTypeIdentifier.heartRate)!,
      HKObjectType.quantityType(forIdentifier: HKQuantityTypeIdentifier.stepCount)!,
      HKObjectType.quantityType(forIdentifier: HKQuantityTypeIdentifier.distanceWalkingRunning)!
    ]
    healthStore.requestAuthorization(toShare: bpmTypes, read: bpmTypes,completion: { (success, error) -> Void in
      if success {
        callback([["status": success], ["error": "Success"]])
      }else{
        if let err = error {
          callback([ ["status": false], ["error": err.localizedDescription] ])
        }else{
          callback([ ["status": false], ["error": "Authorization failed"]])
        }
      }
    })
  }
  
  @objc private func saveMockHeartData() {
    // 1. Create a heart rate BPM Sample
    let heartRateType = HKQuantityType.quantityType(forIdentifier: HKQuantityTypeIdentifier.heartRate)!
    let heartRateQuantity = HKQuantity(unit: HKUnit(from: "count/min"),doubleValue: Double(arc4random_uniform(80) + 100))
    let heartSample = HKQuantitySample(type: heartRateType,quantity: heartRateQuantity, start: Date() , end: Date())
    
    // 2. Save the sample in the store
    healthStore.save(heartSample, withCompletion: { (success, error) -> Void in
      if let error = error {
        print("Error saving heart sample: \(error.localizedDescription)")
      }else{
        print("Saved heart beat to health kit")
      }
    })
  }
  
  private func startMockHeartData() {
    DispatchQueue.main.async {
      self.timer = Timer.scheduledTimer(timeInterval: 30.0,
                                        target: self,
                                        selector: #selector(self.saveMockHeartData),
                                        userInfo: nil,
                                        repeats: true)
    }
  }
  
  private func stopMockHeartData() {
    self.timer?.invalidate()
  }
}

// MARK:- Start Heart rate
extension HealthKitModule { 
  @objc public func startFanEngageHeart(){
    print("getHeartBeat Initally ")
    self.startMockHeartData()
    self.observerHeartRateSample()
    DispatchQueue.main.async {
      self.heartRateTime = Timer.scheduledTimer(withTimeInterval: 30, repeats: true) { (Timer) in
        self.observerHeartRateSample()
      }
    } 
  }
  
  @objc public func stopFanEngageHeart(){
    if self.heartRateTime != nil {
      self.heartObserverQuery = nil
      self.heartRateTime?.invalidate()
      self.heartRateTime = nil
    }
  }
  
  private func observerHeartRateSample(){
    if let hearRateSampleType = HKObjectType.quantityType(forIdentifier: .heartRate) {
      if let heartObserverQuery = self.heartObserverQuery {
        healthStore.stop(heartObserverQuery)
      }
      self.heartObserverQuery = HKObserverQuery(sampleType: hearRateSampleType, predicate: nil, updateHandler: { (_, _, error) in
        if let err = error {
          print(err)
          self.onHeartRateChanged(0, status: false, error: err)
          return
        }else{
          self.fetchLatestHeartSample { (sample) in
            print(sample ?? "No Sample Heart rate not found " )
            if let sample = sample  {
              let heartRate = sample.quantity.doubleValue(for: HKUnit(from: "count/min"))
              self.onHeartRateChanged(heartRate, status: true, error: nil)
            }else{
              print("Else heart rate not found")
              self.onHeartRateChanged(0, status: false, error: nil)
            }
          }
        }
      })
      
      if let heartObserverQuery = self.heartObserverQuery {
        healthStore.execute(heartObserverQuery)
      }
    }
  }
  
  private func fetchLatestHeartSample(completionHandler: @escaping (_ sample: HKQuantitySample?)->Void ){
    if let sampleType = HKObjectType.quantityType(forIdentifier: .heartRate) {
      let datePredicate = HKQuery.predicateForSamples(withStart: Date.distantPast, end: Date(), options: .strictEndDate)
      let sortDescriptor = NSSortDescriptor(key: HKSampleSortIdentifierStartDate, ascending: false)
      let query = HKSampleQuery(sampleType: sampleType, predicate: datePredicate, limit: HKObjectQueryNoLimit, sortDescriptors: [sortDescriptor]) { (_, results, error) in
        if let error = error {
          print("Error: \(error.localizedDescription)")
          return
        }
        if let result = results?.first as? HKQuantitySample{
          completionHandler(result)
        }else{
          completionHandler(nil)
        }
      }
      healthStore.execute(query)
    }else{
      completionHandler(nil)
    }
  }
}

// MARK:- Start Steps rate
extension HealthKitModule {
  
  @objc public func startFanEngageSteps(){
    print("steps Initally ")
    self.observerStepsRateSample()
    DispatchQueue.main.async {
      self.stepsRateTime = Timer.scheduledTimer(withTimeInterval: 30, repeats: true) { (Timer) in
        self.observerStepsRateSample()
      }
    }
  }
  
  @objc public func stopFanEngageSteps(){
    if self.stepsRateTime != nil {
      self.stepObserverQuery = nil
      self.stepsRateTime?.invalidate()
      self.stepsRateTime = nil
    }
  }
  
  private func observerStepsRateSample(){
    if let stepSampleType = HKObjectType.quantityType(forIdentifier: .stepCount) {
      if let stepObserverQuery = self.stepObserverQuery {
        healthStore.stop(stepObserverQuery)
      }
      self.stepObserverQuery = HKObserverQuery(sampleType: stepSampleType, predicate: nil, updateHandler: { (_, _, error) in
        if let err = error {
          print(err)
          self.onStepsChanged(0, status: true, error: err)
          return
        }else{
          self.fetchLatestStepsSample { (sample) in
            print(sample ?? "No Sample Step rate not found " )
            if let sample = sample  {
              let steps = sample.quantity.doubleValue(for: HKUnit.count())
              self.onStepsChanged(Int(steps), status: false, error: nil)
            }else{
              print("Else heart rate not found")
              self.onStepsChanged(0, status: true, error: nil)
            }
          }
        }
      })
      
      if let stepObserverQuery = self.stepObserverQuery {
        healthStore.execute(stepObserverQuery)
      }
    }
  }
  
  private func fetchLatestStepsSample(completionHandler: @escaping (_ sample: HKQuantitySample?)->Void ){
    if let sampleType = HKObjectType.quantityType(forIdentifier: .stepCount) {
      let datePredicate = HKQuery.predicateForSamples(withStart: Date.distantPast, end: Date(), options: .strictEndDate)
      let sortDescriptor = NSSortDescriptor(key: HKSampleSortIdentifierStartDate, ascending: false)
      let query = HKSampleQuery(sampleType: sampleType, predicate: datePredicate, limit: HKObjectQueryNoLimit, sortDescriptors: [sortDescriptor]) { (_, results, error) in
        if let error = error {
          print("Error: \(error.localizedDescription)")
          return
        }
        if let result = results?.first as? HKQuantitySample{
          completionHandler(result)
        }else{
          completionHandler(nil)
        }
      }
      healthStore.execute(query)
    }else{
      completionHandler(nil)
    }
  }
  
  
  func getTodaysSteps(completion: @escaping (Double) -> Void) {
    let stepsQuantityType = HKQuantityType.quantityType(forIdentifier: .stepCount)!
    
    let now = Date()
    let startOfDay = Calendar.current.startOfDay(for: now)
    let predicate = HKQuery.predicateForSamples( withStart: startOfDay, end: now, options: .strictStartDate)
    
    let query = HKStatisticsQuery(
      quantityType: stepsQuantityType,
      quantitySamplePredicate: predicate,
      options: .cumulativeSum
    ) { _, result, _ in
      guard let result = result, let sum = result.sumQuantity() else {
        completion(0.0)
        return
      }
      completion(sum.doubleValue(for: HKUnit.count()))
    }
    healthStore.execute(query)
  }
}
