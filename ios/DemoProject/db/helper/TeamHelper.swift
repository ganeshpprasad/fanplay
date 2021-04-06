//
//  TeamHelper.swift
//  DemoProject
//
//  Created by Sysfore on 28/March/2021.
//

import UIKit
import SQLite

class TeamHelper: DataHelperProtocol {
  
  static func insert(userId:String,item: TeamData) throws -> Bool {
    guard let db = DTDatabase.shared?.db else{
      throw DataAccessError.Datastore_Connection_Error
    }
    var success = true
    let setter = [
      teamid <- Int64(item.teamid!),
      teamName <- (item.teamname != nil ) ? item.teamname! : "",
      sportname <- (item.sportname != nil ) ? item.sportname! : "" ,
      tournamentname <- (item.tournamentname != nil ) ? item.tournamentname! : "" ,
      teamlogourl <-  (item.teamlogourl != nil ) ? item.teamlogourl! : "" ,
      teamstoreurl <- (item.teamstoreurl != nil ) ? item.teamstoreurl! : "" ,
      teamIdServer <- 0,
      lastUpdated <- AppConstants.currentTimeInMiliseconds(),
      lastSynced <- AppConstants.currentTimeInMiliseconds(),
      sId <- userId
    ]
    
    let updateUser = table.filter( sId == userId && teamid == Int64(item.teamid!))
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
  
  static func insert(item: [TeamData]) throws -> Bool {
    return true
  }
  
  typealias T = TeamData
  static let TABLE_NAME = "Team"
  static let table = Table(TABLE_NAME)
  
  static func createTable() throws {
    guard let db = DTDatabase.shared?.db else{
      throw DataAccessError.Datastore_Connection_Error
    }
    do {
      try db.run(table.create(ifNotExists: true) {t in
        t.column(id, primaryKey: .autoincrement)
        t.column(teamid)
        t.column(teamName)
        t.column(sportname)
        t.column(tournamentname)
        t.column(teamlogourl)
        t.column(teamstoreurl)
        t.column(teamIdServer) 
        t.column(lastUpdated)
        t.column(lastSynced)
        t.column(affiliationid)
        t.column(affiliationname)
        t.column(affiliationlogo)
        t.column(sId)
      })
    } catch {
      AppConstants.log(error.localizedDescription)
      throw DataAccessError.Table_Creation_Error
    }
  }
  
  static func getCurrentTeam(tId:Int) -> [TeamData]{
    guard let db = DTDatabase.shared?.db else{
      return []
    }
    let userId = BTUserDefaults.shared.getString(key: .SID)
    let query = table.filter(
      sid  == userId && teamid == Int64(tId)
    )
    do{
      return getUser(userRowList: try db.prepare(query))
    }catch {
      AppConstants.log(error)
    }
    return []
  }
  
  fileprivate static func getUser(userRowList:AnySequence<Row>)->[TeamData]{
    var dataList:[TeamData] = []
    for row in userRowList {
      var item = TeamData.init()
      item.teamid = Int(row[teamid])
      item.teamname  = row[teamName]
      item.sportname  = row[sportname]
      item.tournamentname  = row[tournamentname]
      item.teamlogourl  = row[teamlogourl]
      item.teamstoreurl  = row[teamstoreurl]
      dataList.append(item)
    }
    return dataList
  }
}
