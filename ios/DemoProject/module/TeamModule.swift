//
//  TeamModule.swift
//  DemoProject
//
//  Created by Sysfore on 06/April/2021.
//

import UIKit
import React

@objc(TeamModule)
class TeamModule: RCTEventEmitter {
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
    return ["teamModuleEvent"]
  }
  
  //MARK:- Team call backs
  @objc public func getDefaultTeam(_ callback: @escaping (RCTResponseSenderBlock)){
    callback([TeamHelper.getCurrentTeam(tId: BTUserDefaults.shared.getInt(key: .TEAM_ID))])
  }
  
  @objc public func getPlayers(_ callback: @escaping (RCTResponseSenderBlock)){
    callback([PlayersHelper.getAllPlayers(tId: BTUserDefaults.shared.getInt(key: .TEAM_ID))])
  } 
}
