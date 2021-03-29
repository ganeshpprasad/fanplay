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
      st = true
      er = "Success"
    }else{
      hr = 0
      st = false
      if let err = error {
        er = err.localizedDescription
      }else{
        er = "Failed to fetch heart rate"
      }
    }
    self.sendCustomEvent(body: [ "heartRate": hr, "status": st, "error": er ])
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
    healthStore.requestAuthorization(toShare: bpmTypes, read: [],completion: { (success, error) -> Void in
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
}

// MARK:- Start Heart rate
extension HealthKitModule { 
  @objc public func getHeartBeat(){
    print("getHeartBeat")
    getTodaysHeartRates()
  }
  
  @objc public func stopHeartRate(){
    
  }
  
  /*Method to get todays heart rate - this only reads data from health kit. */
  private func getTodaysHeartRates() {
    print("getTodaysHeartRates")
    //predicate
    //    let calendar = NSCalendar.current
    //    let now = NSDate()
    //    let components = calendar.dateComponents([.year, .month, .day], from: now as Date)
    //
    //    guard let startDate:NSDate = calendar.date(from: components) as NSDate? else { return }
    //
    //    var dayComponent    = DateComponents()
    //    dayComponent.day    = 1
    //    let endDate:NSDate? = calendar.date(byAdding: dayComponent, to: startDate as Date) as NSDate?
    
    //    let predicate = HKQuery.predicateForSamples(withStart: startDate as Date, end: endDate as Date?, options: [])
    let predicate = HKQuery.predicateForSamples(withStart: Date.distantPast,end: Date(), options: [])
    
    //descriptor
    let sortDescriptors = [ NSSortDescriptor(key: HKSampleSortIdentifierEndDate, ascending: false)]
    let heartRateType = HKQuantityType.quantityType(forIdentifier: HKQuantityTypeIdentifier.heartRate)!
    
    let heartRateQuery = HKSampleQuery(sampleType: heartRateType, predicate: predicate, limit: 10, sortDescriptors: sortDescriptors, resultsHandler: { (query, results, error) in
      if let err = error {
        print("Error requesting for access \(err.localizedDescription)")
        self.onHeartRateChanged(0, status: false, error: err)
      }else{
        self.printHeartRateInfo(results: results)
      }
    }) //eo-query
    healthStore.execute(heartRateQuery)
  }//eom
  
  /*used only for testing, prints heart rate info */
  private func printHeartRateInfo(results:[HKSample]?){
    print("printHeartRateInfo")
    if let results = results, results.count > 0 {
      print(results)
      var hearRate:[Double] = []
      for (_, sample) in results.enumerated() {
        if let currData:HKQuantitySample = sample as? HKQuantitySample{
          hearRate.append(currData.quantity.doubleValue(for: HKUnit(from: "count/min")))
          print("[\(sample)]")
          print("Heart Rate: \(currData.quantity.doubleValue(for: HKUnit(from: "count/min")))")
          print("quantityType: \(currData.quantityType)")
          print("Start Date: \(currData.startDate)")
          print("End Date: \(currData.endDate)")
          print("Metadata: \(String(describing: currData.metadata))")
          print("UUID: \(currData.uuid)")
          print("Source: \(currData.sourceRevision)")
          print("Device: \(String(describing: currData.device))")
          print("---------------------------------\n")
        }else{
          print("---------------------------------\n")
        }
      }
      self.onHeartRateChanged(hearRate.average, status: true, error: nil)
    }else{
      self.onHeartRateChanged(0, status: false, error: nil)
    }
  }//eom
}

// MARK:- Start Steps rate
extension HealthKitModule {
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
