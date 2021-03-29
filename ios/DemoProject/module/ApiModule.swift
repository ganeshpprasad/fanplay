//
//  ApiModule.swift
//  DemoProject
//
//  Created by Sysfore on 28/March/2021.
//

import UIKit
import React
import Alamofire

private let BASE_URL = "https://fanplaygurudevapi.azurewebsites.net/api/"
private let VALIDATE_LOGIN = "Login/ValidateSignIn"
private let USER_DETAIL_BY_ID_TOKEN = "User/GetAllUserDetailsByIdToken"
private let PLAYERS_DATA = "TeamDetails/GetTeamPlayersData"
private let FAN_EMOTE = "FanEngagement/GetFanEmote"
private let FE_DETAILS_BY_TEAM_ID = "Dashboard/GetFEDetailsByTeamId"
private let SYNC_FAN_ENGAGEMENT = "FanEngagement/SyncFanEngagement"

class ApiModule: RCTEventEmitter {
  
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
    return ["apiCallEvent"]
  }
}

extension ApiModule {
  @objc func validateSignIn(roleId:String, displayName:String,latitiude:Double, longitude: Double, callback: @escaping (RCTResponseSenderBlock)){
    let parameters: [String : Any] = [
      "roleid": roleId,
      "displayname": displayName,
      "latitiude": latitiude,
      "longitude": longitude
    ]
    AF.request(BASE_URL + VALIDATE_LOGIN, method: .post, parameters: parameters, encoding: JSONEncoding.default).responseJSON { (response) in
      
    }
  }
  
  @objc func getAllUserDetailsByIdToken(callback: @escaping (RCTResponseSenderBlock)){
    AF.request(BASE_URL + USER_DETAIL_BY_ID_TOKEN, method: .get,  encoding: JSONEncoding.default).responseJSON { (response) in
      
    }
  }
  
  @objc func getTeamPlayersData(teamId:String, callback: @escaping (RCTResponseSenderBlock)){
    AF.request(BASE_URL + PLAYERS_DATA + "?teamId=\(teamId)", method: .get,  encoding: URLEncoding.queryString).responseJSON { (response) in
      
    }
  }
  
  @objc func getTeamPlayersData(teamId:String,duration:Int,callback: @escaping (RCTResponseSenderBlock)){
    AF.request(BASE_URL + FAN_EMOTE + "?teamId=\(teamId)&duration=\(duration)", method: .get,  encoding: URLEncoding.queryString).responseJSON { (response) in
      
    }
  }
  
  @objc func getFEDetailsByTeamId(teamId:String,duration:Int,callback: @escaping (RCTResponseSenderBlock)){
    AF.request(BASE_URL + FE_DETAILS_BY_TEAM_ID + "?teamId=\(teamId)", method: .get,  encoding: URLEncoding.queryString).responseJSON { (response) in
      
    }
  }
  
  @objc func syncFanEngagement(){
    let parameters: [String : Any] =     [
      "sid": 0,
      "hrcount": 0,
      "hrdevicetype": 0,
      "datacollectedts": "2021-03-29T05:08:35.092Z",
      "teamcheered": 0,
      "tapcounts": 0,
      "wavecounts": 0,
      "whistlesredeemed": 0,
      "whistlecounts": 0,
      "fescore": 0,
      "points": 0,
      "latitude": 0,
      "longitude": 0,
      "devicemacid": "string",
      "hrzoneid": 0,
      "affiliationid": 0,
      "playertapcheer": [
        [
          "playerid": 0,
          "tapvalue": 0
        ]
      ],
      "playerwavecheer": [
        [
          "playerid": 0,
          "wavevalue": 0
        ]
      ],
      "playerwhistleredeemed": [
        [
          "playerid": 0,
          "wrvalue": 0
        ]
      ]
    ]
    AF.request(BASE_URL + SYNC_FAN_ENGAGEMENT, method: .post, parameters: parameters, encoding: JSONEncoding.default).responseJSON { (response) in
      
    }
  }
  
}
