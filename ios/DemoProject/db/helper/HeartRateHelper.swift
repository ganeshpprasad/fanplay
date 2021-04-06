//
//  HeartRateHelper.swift
//  DemoProject
//
//  Created by Sysfore on 28/March/2021.
//

import UIKit
import SQLite

class HeartRateHelper:  DataHelperProtocol {
  
  static func insert(item: HeartRateModel) throws -> Bool {
    guard let db = DTDatabase.shared?.db else{
      throw DataAccessError.Datastore_Connection_Error
    }
    var success = true
    let setter = [
      heartRate <- Int64(item.heartRate),
      type <- Int64(item.type),
      sId <- item.sId,
      lastSynced <- AppConstants.currentTimeInMiliseconds(),
      lastUpdated <- AppConstants.currentTimeInMiliseconds(),
    ] 
    do {
      let insert = table.insert(setter)
      let insertRowId = try db.run(insert)
      success = insertRowId > 0
    } catch {
      AppConstants.log(error.localizedDescription)
      throw DataAccessError.Insert_Error
    }
    return success
  }
  
  static func insert(item: [HeartRateModel]) throws -> Bool {
    return true
  }
  
  typealias T = HeartRateModel
  static let TABLE_NAME = "HeartRate"
  static let table = Table(TABLE_NAME)
  
  static func createTable() throws {
    guard let db = DTDatabase.shared?.db else{
      throw DataAccessError.Datastore_Connection_Error
    }
    do {
      try db.run(table.create(ifNotExists: true) {t in
        t.column(id, primaryKey: .autoincrement)
        t.column(heartRate)
        t.column(type)
        t.column(lastUpdated)
        t.column(lastSynced)
        t.column(sId)
      })
    } catch {
      AppConstants.log(error.localizedDescription)
      throw DataAccessError.Table_Creation_Error
    }
  }
}
