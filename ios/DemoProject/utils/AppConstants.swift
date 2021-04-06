//
//  AppConstants.swift
//  DemoProject
//
//  Created by Sysfore on 28/March/2021.
//

import UIKit


//let BASE_URL = "https://fanplaygurudevapi.azurewebsites.net/api/"
let BASE_URL = "https://fangurudevadb2capi.azurewebsites.net/api/"
let VALIDATE_LOGIN = "Login/ValidateSignIn"
let USER_DETAIL_BY_ID_TOKEN = "User/GetAllUserDetailsByIdToken"
let PLAYERS_DATA = "TeamDetails/GetTeamPlayersData"
let FAN_EMOTE = "FanEngagement/GetFanEmote"
let FE_DETAILS_BY_TEAM_ID = "Dashboard/GetFEDetailsByTeamId"
let CREATE_FAN_ENGAGEMENT = "FanEngagement/CreateFanEngagement"
let SYNC_FAN_ENGAGEMENT = "FanEngagement/SyncFanEngagement"

class AppConstants {
  static func log(_ items: Any..., separator: String = " ", terminator: String = "\n"){
    //        #if DEBUG
    //        print(items,separator,terminator)
    //        #endif
    print(items,separator,terminator)
  }
  static func getCurrentDate()->String{
    let dateFormatter = DateFormatter()
    dateFormatter.dateFormat = "dd-MM-yyyy HH:mm:ss"
    return dateFormatter.string(from: Date())
  }
  
  static func getFormattedCurrentDate()->String{
    let dateFormatter = DateFormatter()
    dateFormatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
    return dateFormatter.string(from: Date())
  }
  
  //Date to milliseconds
  static func currentTimeInMiliseconds() -> Double {
    let currentDate = NSDate()
    let dateFormatter = DateFormatter()
    dateFormatter.dateFormat = "dd-MM-yyyy HH:mm:ss"
    dateFormatter.timeZone = NSTimeZone(name: "UTC") as TimeZone?
    let date = dateFormatter.date(from: dateFormatter.string(from: currentDate as Date))
    return date!.timeIntervalSince1970
  }
  
  static func getDeviceInfo() -> String {
    return "VERSION.RELEASE : \(UIDevice.current.systemVersion), MODEL : \(UIDevice.current.model)"
  }
}
//Milliseconds to date
extension Int {
  func dateFromMilliseconds(format:String) -> Date {
    let date : NSDate! = NSDate(timeIntervalSince1970:Double(self) / 1000.0)
    let dateFormatter = DateFormatter()
    dateFormatter.dateFormat = format
    dateFormatter.timeZone = TimeZone.current
    let timeStamp = dateFormatter.string(from: date as Date)
    
    let formatter = DateFormatter()
    formatter.dateFormat = format
    return ( formatter.date( from: timeStamp ) )!
  }
}

