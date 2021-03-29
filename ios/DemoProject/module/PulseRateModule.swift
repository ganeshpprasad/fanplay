//
//  PulseRateModule.swift
//  DemoProject
//
//  Created by Sysfore on 26/March/2021.
//

import UIKit
import React 

@objc(PulseRateModule)
class PulseRateModule: RCTEventEmitter {
  
  @objc override init() {
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
    return ["cameraHeartRateEvent"]
  }
  
  @objc public func getPulseRate(){
    DispatchQueue.main.async {
      if let appDelegate = UIApplication.shared.delegate as? AppDelegate {
        appDelegate.goToPulseViewController(pulseViewDelegate: self)
      }else{
        print("App Delegate not found ")
        self.sendEvent(withName: "cameraHeartRateEvent", body: [ "heartRate":0,
                                                                 "status": false,
                                                                 "error": "Failed to capture the heart rate"])
      }
    }
  }
}

extension PulseRateModule: PulseViewDelegate {
  func onHeartRateReceved(_ heartRate: Any) {
    DispatchQueue.main.async {
      self.sendEvent(withName: "cameraHeartRateEvent", body: heartRate)
    }
  }
}
