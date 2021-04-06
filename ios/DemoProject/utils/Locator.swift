//
//  Locator.swift
//  DemoProject
//
//  Created by Sysfore on 29/March/2021.
//

import UIKit
import CoreLocation

class Locator: NSObject, CLLocationManagerDelegate {
  
  static let shared: Locator = Locator()
  
  typealias Callback = (Locator?, Error?) -> Void
  
  var requests: Array <Callback> = Array <Callback>()
  
  var location: CLLocation? { return sharedLocationManager.location  }
  
  lazy var sharedLocationManager: CLLocationManager = {
    let newLocationmanager = CLLocationManager()
    newLocationmanager.delegate = self
    return newLocationmanager
  }()
  
  // MARK: - Authorization
  class func authorize() { shared.authorize() }
  
  func authorize() { sharedLocationManager.requestWhenInUseAuthorization() }
  
  // MARK: - Helpers
  func locate(callback:  @escaping (Callback)) {
    self.requests.append(callback)
    sharedLocationManager.startUpdatingLocation()
  }
  
  func reset() {
    self.requests = Array <Callback>()
    sharedLocationManager.stopUpdatingLocation()
  }
  
  // MARK: - Delegate
  func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
    for request in self.requests { request(nil,error) }
    self.reset()
  }
  
  func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
    for request in self.requests { request(self,nil) }
    self.reset()
  }
}
