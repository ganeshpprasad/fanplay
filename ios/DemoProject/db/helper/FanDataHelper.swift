//
//  FanDataHelper.swift
//  DemoProject
//
//  Created by Sysfore on 28/March/2021.
//

import UIKit
import SQLite

class FanDataHelper: DataHelperProtocol {
  static func insert(item: [FanDataModel]) throws -> Bool {
    return true
  }
  
  typealias T = FanDataModel
  static let TABLE_NAME = "FanData"
  static let table = Table(TABLE_NAME)
  
  static func createTable() throws {
    guard let db = DTDatabase.shared?.db else{
      throw DataAccessError.Datastore_Connection_Error
    }
    do {
      try db.run(table.create(ifNotExists: true) {t in
        t.column(id, primaryKey: .autoincrement)
        t.column(totalTapCount)
        t.column(fanMetric)
        t.column(totalPoints)
        t.column(flag)
        t.column(playerId)
        t.column(teamid)
        t.column(lastUpdated)
        t.column(lastSynced)
        t.column(sId)
      })
    } catch {
      AppConstants.log(error.localizedDescription)
      throw DataAccessError.Table_Creation_Error
    }
  }
  
  static func updateTapCount(count: Int) throws -> Bool {
    
    guard let db = DTDatabase.shared?.db else{
      throw DataAccessError.Datastore_Connection_Error
    }
    var success = true
    
    let userId = BTUserDefaults.shared.getString(key: .SID)
    let tId = BTUserDefaults.shared.getInt(key: .TEAM_ID)
    
    let currentCount = getCurrentCount(userId: userId, teamId: tId)
    let setter = [
      totalTapCount <- Int64(currentCount + count),
      playerId <- Double(0),
      totalPoints <- Double(0),
      teamid <- Int64(tId),
      flag <- 0,
      lastUpdated <- AppConstants.currentTimeInMiliseconds(),
      lastSynced <- AppConstants.currentTimeInMiliseconds(),
      sId <- userId
    ]
    let updateUser = table.filter( teamid == Int64(tId) && sId == userId)
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
  
  static func getCurrentCount(userId:String, teamId: Int) -> Int{
    guard let db = DTDatabase.shared?.db else{
      return 0
    } 
    let query = table.filter(
      teamid == Int64(teamId) && sId == userId
    )
    
    do{
      let data = try db.prepare(query)
      var count = 0
      for row in data {
        count = Int(row[totalTapCount])
      }
      return count
    }catch {
      AppConstants.log(error)
    }
    return 0
  }
  
//  fileprivate static func getUser(userRowList:AnySequence<Row>)->[TeamData]{
//    var dataList:[TeamData] = []
//    for row in userRowList {
//      var item = TeamData.init()
//      item.teamid = Int(row[teamid])
//      item.teamname  = row[teamName]
//      item.sportname  = row[sportname]
//      item.tournamentname  = row[tournamentname]
//      item.teamlogourl  = row[teamlogourl]
//      item.teamstoreurl  = row[teamstoreurl]
//      dataList.append(item)
//    }
//    return dataList
//  }
}
