//
//  UsageAnalyticsHelper.swift
//  DemoProject
//
//  Created by Sysfore on 28/March/2021.
//

import UIKit
import SQLite

class UsageAnalyticsHelper: DataHelperProtocol {
  static func insert(item: [UsageAnalyticsModel]) throws -> Bool {
    return true
  }
  
  typealias T = UsageAnalyticsModel
  static let TABLE_NAME = "UsageAnalytics"
  static let table = Table(TABLE_NAME)
  
  static func createTable() throws {
    guard let db = DTDatabase.shared?.db else{
      throw DataAccessError.Datastore_Connection_Error
    }
    do {
      try db.run(table.create(ifNotExists: true) {t in
        t.column(id, primaryKey: .autoincrement)
        t.column(typeStr)
        t.column(value) 
      })
    } catch {
      AppConstants.log(error.localizedDescription)
      throw DataAccessError.Table_Creation_Error
    }
  }
}
