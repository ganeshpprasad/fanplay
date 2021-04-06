//
//  PendingFEHelper.swift
//  DemoProject
//
//  Created by Sysfore on 05/April/2021.
//

import UIKit
import SQLite

class PendingFEHelper : DataHelperProtocol {
  
  static func insert(item: PendingFE) throws -> Int {
    guard let db = DTDatabase.shared?.db else{
      throw DataAccessError.Datastore_Connection_Error
    }
    let userId = BTUserDefaults.shared.getString(key: .SID)
    let setter = [
      json <- item.json,
      synced <- false,
      lastUpdated <- AppConstants.currentTimeInMiliseconds(),
      sId <- userId,
    ]
    var insertRowId = -1
    do {
      let insert = table.insert(setter)
      insertRowId = Int(try db.run(insert))
    } catch {
      AppConstants.log(error.localizedDescription)
      throw DataAccessError.Insert_Error
    }
    return insertRowId
  }
  
  static func insert(item: [PendingFE]) throws -> Bool {
    return true
  }
  
  typealias T = PendingFE
  static let TABLE_NAME = "PendingFE"
  static let table = Table(TABLE_NAME)
  
  static func createTable() throws {
    guard let db = DTDatabase.shared?.db else{
      throw DataAccessError.Datastore_Connection_Error
    }
    do {
      try db.run(table.create(ifNotExists: true) {t in
        t.column(id, primaryKey: .autoincrement)
        t.column(json)
        t.column(synced)
        t.column(sId)
        t.column(lastUpdated)
      })
    } catch {
      AppConstants.log(error.localizedDescription)
      throw DataAccessError.Table_Creation_Error
    }
  }
  
  static func getAllPendingFe() -> [PendingFE]{
    guard let db = DTDatabase.shared?.db else{
      return []
    }
    let userId = BTUserDefaults.shared.getString(key: .SID)
    let query = table.filter( sid  == userId && synced == false)
    do{
      return getFe(dataList: try db.prepare(query))
    }catch {
      AppConstants.log(error)
    }
    return []
  }
  
  fileprivate static func getFe(dataList:AnySequence<Row>)->[PendingFE]{
    var data:[PendingFE] = []
    for row in dataList {
      var item = PendingFE.init()
      item.id  = Int(row[id])
      item.json = row[json]
      data.append(item)
    }
    return data
  }
  
  static func updateSyncedTrue(itemId: Int) throws -> Bool {
    guard let db = DTDatabase.shared?.db else{
      throw DataAccessError.Datastore_Connection_Error
    }
    var success = true
    let userId = BTUserDefaults.shared.getString(key: .SID)
    let setter = [
      synced <- true
    ]
    let updateUser = table.filter( id == Int64(itemId) && sId == userId)
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


