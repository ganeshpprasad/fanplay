//
//  ActivityHelper.swift
//  DemoProject
//
//  Created by Sysfore on 28/March/2021.
//

import UIKit
import SQLite

class ActivityHelper: DataHelperProtocol {
  static func insert(item: [ActivityModel]) throws -> Bool {
    return true
  }
  
  typealias T = ActivityModel
  static let TABLE_NAME = "Activity"
  static let table = Table(TABLE_NAME)
  
  static func createTable() throws {
    guard let db = DTDatabase.shared?.db else{
      throw DataAccessError.Datastore_Connection_Error
    }
    do {
      try db.run(table.create(ifNotExists: true) {t in
        t.column(id, primaryKey: .autoincrement)
        t.column(activityType)
        t.column(scdJson)
        t.column(hrJson)
        t.column(bpJson)
        t.column(started)
        t.column(ended)
      })
    } catch {
      AppConstants.log(error.localizedDescription)
      throw DataAccessError.Table_Creation_Error
    }
  }
}
