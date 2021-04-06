//
//  WhistleDataHelper.swift
//  DemoProject
//
//  Created by Sysfore on 28/March/2021.
//

import UIKit
import SQLite

class WhistleDataHelper: DataHelperProtocol {
  
  static func insert(item: [WhistleDataModel]) throws -> Bool {
    return true
  }
  
  typealias T = WhistleDataModel
  static let TABLE_NAME = "WhistleData"
  static let table = Table(TABLE_NAME)
  
  static func createTable() throws {
    guard let db = DTDatabase.shared?.db else{
      throw DataAccessError.Datastore_Connection_Error
    }
    do {
      try db.run(table.create(ifNotExists: true) {t in
        t.column(id, primaryKey: .autoincrement)
        t.column(whistleCount)
        t.column(whistleEarned)
        t.column(whistleRedeemed)
        t.column(whistleType)
        t.column(lastUpdated)
        t.column(lastSynced)
        t.column(teamid)
        t.column(sId)
      })
    } catch {
      AppConstants.log(error.localizedDescription)
      throw DataAccessError.Table_Creation_Error
    }
  }
  
  static func updateWhistleCount(count:Int) throws -> Bool {
    guard let db = DTDatabase.shared?.db else{
      throw DataAccessError.Datastore_Connection_Error
    }
    var success = true
    let userId = BTUserDefaults.shared.getString(key: .SID)
    let tId = BTUserDefaults.shared.getInt(key: .TEAM_ID)
    
    
    let whistleCoun = getCurrentCount(userId: userId,teamId:tId, position: 0)
    let whistleEarne = getCurrentCount(userId: userId,teamId:tId, position: 1)
    let whistleRedeeme = getCurrentCount(userId: userId,teamId:tId, position: 2)
    
    let setter = [
      whistleCount <- Int64(whistleCoun + count),
      whistleEarned <- Int64(whistleEarne - count),
      whistleRedeemed <- Int64(whistleRedeeme + count),
      whistleType <- Int64(0),
      sId <- userId,
      teamid <- Int64(tId),
      lastUpdated <- AppConstants.currentTimeInMiliseconds(),
      lastSynced <- AppConstants.currentTimeInMiliseconds(),
    ]
    
    let updateUser = table.filter( sId == userId && teamid == Int64(tId))
    
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
  
  static func getCurrentCount(userId:String, teamId:Int, position:Int) -> Int{
    guard let db = DTDatabase.shared?.db else{
      return 0
    }
    let query = table.filter(sId == userId && teamid == Int64(teamId))
    
    do{
      let data = try db.prepare(query)
      var count = 0
      for row in data {
        if position == 0 {
          count = Int(row[whistleCount])
        }
        
        if position == 1 {
          count = Int(row[whistleEarned])
        }
        
        if position == 2 {
          count = Int(row[whistleRedeemed])
        }
        
      }
      return count
    }catch {
      AppConstants.log(error)
    }
    return 0
  }
  
  static func updateWhistleEarnedCountFromFe(feDetails:FeDetails) throws -> Bool {
    guard let db = DTDatabase.shared?.db else{
      throw DataAccessError.Datastore_Connection_Error
    }
    var success = true
    let userId = BTUserDefaults.shared.getString(key: .SID)
    let tId = BTUserDefaults.shared.getInt(key: .TEAM_ID)
     
    
    let setter = [
      whistleEarned <- Int64(feDetails.whistlecounts  ),
      lastUpdated <- AppConstants.currentTimeInMiliseconds(),
      lastSynced <- AppConstants.currentTimeInMiliseconds(),
    ]
    
    let updateUser = table.filter( sId == userId && teamid == Int64(tId))
    
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
  
}
