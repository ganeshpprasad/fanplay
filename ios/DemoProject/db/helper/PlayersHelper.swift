//
//  PlayersHelper.swift
//  DemoProject
//
//  Created by Sysfore on 28/March/2021.
//

import UIKit
import SQLite

class PlayersHelper: DataHelperProtocol {
  
  static func insert(userId:String , tId:String, item:PlayerData) throws -> Bool {
    guard let db = DTDatabase.shared?.db else{
      throw DataAccessError.Datastore_Connection_Error
    }
    var success = true
    let setter = [
      teamid <- Int64(tId)!,
      playerId <- Double(item.playerid!),
      playerName <- (item.playername != nil) ? item.playername! : "",
      isPlaying <- (item.isplaying != nil) ? item.isplaying! : false,
      isPlayerActive <- (item.isplayeractive != nil) ? item.isplayeractive! : false,
      playerimagepath <- (item.playerimagepath != nil) ? item.playerimagepath! : "",
      lastUpdated <- AppConstants.currentTimeInMiliseconds(),
      lastSynced <- AppConstants.currentTimeInMiliseconds(),
      tapCount <- 0,
      waveCount <- 0,
      whistleCount <- 0,
      sId <- userId
    ]
    
    
    let updateUser = table.filter( sId == userId && teamid == Int64(tId)! && playerId == Double(item.playerid!))
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
      print(error)
      AppConstants.log(error.localizedDescription)
      throw DataAccessError.Insert_Error
    }
    return success
  }
  
  static func insert(item: [PlayerData]) throws -> Bool {
    return true
  }
  
  typealias T = PlayerData
  static let TABLE_NAME = "Players"
  static let table = Table(TABLE_NAME)
  
  static func createTable() throws {
    guard let db = DTDatabase.shared?.db else{
      throw DataAccessError.Datastore_Connection_Error
    }
    do {
      try db.run(table.create(ifNotExists: true) {t in
        t.column(id, primaryKey: .autoincrement)
        t.column(teamid)
        t.column(playerId) 
        t.column(playerName)
        t.column(playerimagepath)
        t.column(isPlaying)
        t.column(isPlayerActive)
        t.column(tapCount)
        t.column(waveCount)
        t.column(whistleCount)
        t.column(lastUpdated)
        t.column(lastSynced)
        t.column(sId)
      })
    } catch {
      AppConstants.log(error.localizedDescription)
      throw DataAccessError.Table_Creation_Error
    }
  }
  
  static func getAllPlayers(tId: Int) -> [PlayerData]{
    guard let db = DTDatabase.shared?.db else{
      return []
    }
    let userId = BTUserDefaults.shared.getString(key: .SID)
    let query = table.filter(
      sid  == userId && teamid == Int64(tId) && isPlaying == true && isPlayerActive == true 
    )
    do{
      return getPlayer(userRowList: try db.prepare(query))
    }catch {
      AppConstants.log(error)
    }
    return []
  }
  
  fileprivate static func getPlayer(userRowList:AnySequence<Row>)->[PlayerData]{
    var dataList:[PlayerData] = []
    for row in userRowList {
      var item = PlayerData.init()
      item.playerid = Int(row[playerId])
      item.playername  = row[playerName]
      item.playerimagepath = row[playerimagepath]
      item.tapCount = Int(row[tapCount])
      item.waveCount = Int(row[waveCount])
      item.whistleCount = Int(row[whistleCount])
      dataList.append(item)
    }
    
    return dataList
  }
   
  static func updateTapCount(count: Int, teamId: Int, playrId: Int) throws -> Bool {
    
    guard let db = DTDatabase.shared?.db else{
      throw DataAccessError.Datastore_Connection_Error
    }
    var success = true
    let userId = BTUserDefaults.shared.getString(key: .SID)
    let currentCount = getCurrentCount(userId: userId, teamId: teamId, playrId: playrId, position: 0)
    let setter = [
      tapCount <- Int64(currentCount + count)
    ]
    let updateUser = table.filter( playerId == Double(playrId) && teamid == Int64(teamId) && sid  == userId)
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
 
  
  static func updateWaveCount(count: Int, teamId: Int, playrId: Int) throws -> Bool {
    
    guard let db = DTDatabase.shared?.db else{
      throw DataAccessError.Datastore_Connection_Error
    }
    var success = true
    let userId = BTUserDefaults.shared.getString(key: .SID)
    let currentCount = getCurrentCount(userId: userId, teamId: teamId, playrId: playrId, position: 1)
    let setter = [
      waveCount <- Int64(currentCount + playrId)
    ]
    let updateUser = table.filter( playerId == Double(playrId) && teamid == Int64(teamId) && sid  == userId)
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
  
  static func updateWhistleCount(count: Int, teamId: Int, playrId: Int) throws -> Bool {
    
    guard let db = DTDatabase.shared?.db else{
      throw DataAccessError.Datastore_Connection_Error
    }
    var success = true
    let userId = BTUserDefaults.shared.getString(key: .SID)
    let currentCount = getCurrentCount(userId: userId, teamId: teamId, playrId: playrId, position: 2)
    let setter = [
      whistleCount <- Int64(currentCount + count)
    ]
    let updateUser = table.filter( playerId == Double(playrId) && teamid == Int64(teamId) && sid  == userId)
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
  
  
  static func getCurrentCount(userId:String, teamId: Int, playrId: Int, position:Int) -> Int{
    guard let db = DTDatabase.shared?.db else{
      return 0
    }
    let query = table.filter(
      playerId == Double(playrId) && teamid == Int64(teamId) && sId == userId
    )
    
    do{
      let data = try db.prepare(query)
      var count = 0
      for row in data {
        if position == 0 {
          count = Int(row[tapCount])
        }
        
        if position == 1 {
          count = Int(row[waveCount])
        }
        
        if position == 2 {
          count = Int(row[whistleCount])
        }
      }
      return count
    }catch {
      AppConstants.log(error)
    }
    return 0
  }
  
  static func updateTapWaveWhistleCountToZero() throws -> Bool {
    
    guard let db = DTDatabase.shared?.db else{
      throw DataAccessError.Datastore_Connection_Error
    }
    var success = true
    let userId = BTUserDefaults.shared.getString(key: .SID)
    let teamId = BTUserDefaults.shared.getInt(key: .TEAM_ID)
    let setter = [
      tapCount <- 0,
      waveCount <- 0,
      whistleCount <- 0
    ]
    let updateUser = table.filter( teamid == Int64(teamId) && sid  == userId)
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
