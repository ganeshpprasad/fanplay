//
//  UserHelper.swift
//  DemoProject
//
//  Created by Sysfore on 28/March/2021.
//

import UIKit
import SQLite

class UserHelper: DataHelperProtocol {
  
  static func insertUserDetails(userDetails:UserDetails) throws -> Bool {
    
    guard let db = DTDatabase.shared?.db else{
      throw DataAccessError.Datastore_Connection_Error
    }
    var success = true
    
    let setter = [
      tokenId <- (userDetails.token != nil ) ? userDetails.token! : "",
      sid <- String((userDetails.sid != nil ) ? userDetails.sid! : 0),
      latitude <- userDetails.latitude!,
      longitude <- userDetails.longitude!,
      timeZone <- "",
      age <- String((userDetails.ageinyears != nil ) ? userDetails.ageinyears! : 0),
      loginType <- String((userDetails.loginType != nil ) ? userDetails.loginType! : 0),
      profileName <- (userDetails.displayname != nil ) ? userDetails.displayname! : "",
      profileImgUrl <- (userDetails.profileimage != nil ) ? userDetails.profileimage! : "" ,
      teamPref <- String((userDetails.teamid != nil ) ? userDetails.teamid! : 0 ),
      gender <- (userDetails.gender != nil ) ? userDetails.gender! : "" ,
      mobile <- (userDetails.mobilenumber != nil ) ? userDetails.mobilenumber! : "" ,
      email <- (userDetails.email != nil ) ? userDetails.email! : "" ,
      dob <- (userDetails.dob != nil ) ? userDetails.dob! : "",
      city <- (userDetails.city != nil ) ? userDetails.city! : "",
      height <- (userDetails.height != nil ) ? userDetails.height! : "" ,
      heightMeasure <- (userDetails.heightmeasure != nil ) ? userDetails.heightmeasure! : "",
      weight <- (userDetails.weight != nil ) ? userDetails.weight! : "",
      weightMeasure <- (userDetails.weightmeasure != nil ) ? userDetails.weightmeasure! : "" ,
      deviceId <- (userDetails.devicemacid != nil ) ? userDetails.devicemacid! : "",
      phoneDeviceInfo <- (userDetails.phonedeviceinfo != nil ) ? userDetails.phonedeviceinfo! : "",
      lastUpdated <- AppConstants.currentTimeInMiliseconds(),
      lastSynced <- AppConstants.currentTimeInMiliseconds(),
      affiliationid <- Int64((userDetails.affiliationid != nil ) ? userDetails.affiliationid! : 0),
      affiliationname <- (userDetails.affiliationname != nil ) ? userDetails.affiliationname! : "",
      affiliationlogo <- (userDetails.affiliationlogo != nil ) ? userDetails.affiliationlogo! : ""
    ]
    
    let updateUser = table.filter( sId == String(userDetails.sid!))
    
    do {
      let updatedRowId = try db.run(updateUser.update(setter))
      if updatedRowId > 0 {
        success = true
      }else{
        let insert = table.insert(setter)
        let insertRowId = try db.run(insert)
        success = insertRowId > 0
      }
    } catch {
      AppConstants.log(error.localizedDescription)
      throw DataAccessError.Insert_Error
    }
    return success
  }
  
  static func insert(item: [UserDetails]) throws -> Bool {
    return true
  }
  
  typealias T = UserDetails
  static let TABLE_NAME = "User"
  static let table = Table(TABLE_NAME)
  
  static func createTable() throws {
    guard let db = DTDatabase.shared?.db else{
      throw DataAccessError.Datastore_Connection_Error
    }
    do {
      try db.run(table.create(ifNotExists: true) {t in
        t.column(id, primaryKey: .autoincrement)
        t.column(tokenId)
        t.column(sid)
        t.column(latitude)
        t.column(longitude)
        t.column(timeZone)
        t.column(age)
        t.column(loginType)
        t.column(profileName)
        t.column(profileImgUrl)
        t.column(teamPref)
        t.column(gender)
        t.column(mobile)
        t.column(email)
        t.column(dob)
        t.column(city)
        t.column(height)
        t.column(heightMeasure)
        t.column(weight)
        t.column(weightMeasure)
        t.column(deviceId)
        t.column(phoneDeviceInfo)
        t.column(affiliationid)
        t.column(affiliationname)
        t.column(affiliationlogo)
        t.column(lastUpdated)
        t.column(lastSynced)
      })
    } catch {
      AppConstants.log(error.localizedDescription)
      throw DataAccessError.Table_Creation_Error
    }
  }
  
  static func getCurrentUser(sId:String) -> [UserDetails]{
    guard let db = DTDatabase.shared?.db else{
      return []
    }
    let userId = BTUserDefaults.shared.getString(key: .SID)
    let query = table.filter(
      sid  == userId
    )
    do{
      return getUser(userRowList: try db.prepare(query))
    }catch {
      AppConstants.log(error)
    }
    return []
  }
  
  fileprivate static func getUser(userRowList:AnySequence<Row>)->[UserDetails]{
    var dataList:[UserDetails] = []
    for row in userRowList {
      var userDetails = UserDetails.init()
      userDetails.token = row[tokenId]
      userDetails.sid = Int(row[sid])
      userDetails.loginType = Int(row[loginType])
      userDetails.latitude = row[latitude]
      userDetails.longitude = row[longitude]
      userDetails.ageinyears = Int(row[age])
      userDetails.displayname = row[profileName]
      userDetails.profileimage = row[profileImgUrl]
      userDetails.teamid = Int(row[teamPref])
      userDetails.gender = row[gender]
      userDetails.mobilenumber = row[mobile]
      userDetails.email = row[email]
      userDetails.dob = row[dob]
      userDetails.city = row[city]
      userDetails.height = row[height]
      userDetails.heightmeasure = row[heightMeasure]
      userDetails.weight = row[weight]
      userDetails.weightmeasure = row[weightMeasure]
      userDetails.devicemacid = row[deviceId]
      userDetails.phonedeviceinfo = row[phoneDeviceInfo]
      dataList.append(userDetails)
    }
    return dataList
  }
  
  static func updateToken(userDetail:UserDetails) throws -> Bool {
    
    guard let db = DTDatabase.shared?.db else{
      throw DataAccessError.Datastore_Connection_Error
    }
    var success = true
    
    let setter = [
      tokenId <- (userDetail.token != nil ) ? userDetail.token! : "",
      phoneDeviceInfo <- AppConstants.getDeviceInfo(),
      lastUpdated <- AppConstants.currentTimeInMiliseconds(),
      lastSynced <- AppConstants.currentTimeInMiliseconds(),
    ]
    let updateUser = table.filter( sId == String(userDetail.sid!))
    do {
      let updatedRowId = try db.run(updateUser.update(setter))
      if updatedRowId > 0 {
        success = true
      }
    } catch {
      AppConstants.log(error.localizedDescription)
      throw DataAccessError.Insert_Error
    }
    return success
  }
}
