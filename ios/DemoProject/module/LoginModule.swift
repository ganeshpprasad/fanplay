//
//  LoginModule.swift
//  DemoProject
//
//  Created by Sysfore on 29/March/2021.
//

import UIKit
import AVFoundation
import React

//Logintype 1- email , 2 -otp, 3 - gmail etc
@objc(LoginModule)
class LoginModule: RCTEventEmitter {
  
  private let homeRepo = HomeRepo.init()
  
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
    return ["loginModuleEvent"]
  } 
  
  @objc func insertOrUpdate(_ tokenId: String, tokenExpires: Int,
                            displayName: String, type: Int
                            ,callback: @escaping (RCTResponseSenderBlock)) {
    
    //insert toekn to user defaults
    BTUserDefaults.shared.insertString(key: .TOKEN, value: tokenId)
    
    //check and insert the user data 
    self.homeRepo.insertOrUpdateUser(tokenId: tokenId, tokenExpires: Double(tokenExpires), displayName: displayName, type: Double(type)) { (result) in
      print(result)
      callback([result])
    }
  }
  
  @objc func insertTeam(_ teamIdServer: Int, callback: @escaping (RCTResponseSenderBlock)) {
    BTUserDefaults.shared.insertInt(key:.TEAM_ID, value: teamIdServer)
    self.homeRepo.insertTeam(teamIdServer) { (result) in
      print(result)
      callback([result])
    }
  }
}
