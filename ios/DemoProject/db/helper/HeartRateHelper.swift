//
//  HeartRateHelper.swift
//  DemoProject
//
//  Created by Sysfore on 28/March/2021.
//

import UIKit
import SQLite

class HeartRateHelper:  DataHelperProtocol {
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
      })
    } catch {
      AppConstants.log(error.localizedDescription)
      throw DataAccessError.Table_Creation_Error
    }
  }
}
