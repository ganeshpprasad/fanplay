//
//  SleepHelper.swift
//  DemoProject
//
//  Created by Sysfore on 28/March/2021.
//

import UIKit
import SQLite

class SleepHelper: DataHelperProtocol {
  static func insert(item: [SleepModel]) throws -> Bool {
    return true
  }
  
  typealias T = SleepModel
  static let TABLE_NAME = "Sleep"
  static let table = Table(TABLE_NAME)
  
  static func createTable() throws {
    guard let db = DTDatabase.shared?.db else{
      throw DataAccessError.Datastore_Connection_Error
    }
    do {
      try db.run(table.create(ifNotExists: true) {t in
        t.column(id, primaryKey: .autoincrement)
        t.column(deviceType)
        t.column(sleepDuration)
        t.column(awake)
        t.column(restless)
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
