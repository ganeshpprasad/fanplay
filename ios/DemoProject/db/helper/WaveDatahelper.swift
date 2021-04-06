//
//  WaveDatahelper.swift
//  DemoProject
//
//  Created by Sysfore on 28/March/2021.
//

import UIKit
import SQLite

class WaveDatahelper: DataHelperProtocol {
  static func insert(item: [WaveDataModel]) throws -> Bool {
    return true
  }
  
  typealias T = WaveDataModel
  static let TABLE_NAME = "WaveData"
  static let table = Table(TABLE_NAME)
  
  static func createTable() throws {
    guard let db = DTDatabase.shared?.db else{
      throw DataAccessError.Datastore_Connection_Error
    }
    do {
      try db.run(table.create(ifNotExists: true) {t in
        t.column(id, primaryKey: .autoincrement)
        t.column(waveCount)
        t.column(type)
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
  
  static func updateWaveCount(count: Int) throws -> Bool {
    
    guard let db = DTDatabase.shared?.db else{
      throw DataAccessError.Datastore_Connection_Error
    }
    var success = true
    let userId = BTUserDefaults.shared.getString(key: .SID)
    let tId = BTUserDefaults.shared.getInt(key: .TEAM_ID)
    let curentCount = getCurrentCount(userId: userId, teamId: tId)
    let setter = [
      waveCount <- Int64(curentCount + count),
      type <- Int64(5),
      lastUpdated <- AppConstants.currentTimeInMiliseconds(),
      lastSynced <- AppConstants.currentTimeInMiliseconds(),
      sId <- userId,
      teamid <- Int64(tId)
    ]
    let updateUser = table.filter( sid  == userId && teamid == Int64(tId))
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
  
  static func getCurrentCount(userId:String, teamId:Int) -> Int{
    guard let db = DTDatabase.shared?.db else{
      return 0
    }
    let query = table.filter(
      sid  == userId && teamid == Int64(teamId)
    )
    
    do{
      let data = try db.prepare(query)
      var count = 0
      for row in data {
        count = Int(row[waveCount])
      }
      return count
    }catch {
      AppConstants.log(error)
    }
    return 0
  }
}
