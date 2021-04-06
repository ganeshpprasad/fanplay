//
//  UserHelper.swift
//  DemoProject
//
//  Created by Sysfore on 28/March/2021.
//

import UIKit
import SQLite

class SCDHelper : DataHelperProtocol {
  
  static func insert(item: [SCDModel]) throws -> Bool {
    return true
  }
  
  typealias T = SCDModel
  static let TABLE_NAME = "SCD"
  static let table = Table(TABLE_NAME)
  
  static func createTable() throws {
    guard let db = DTDatabase.shared?.db else{
      throw DataAccessError.Datastore_Connection_Error
    }
    do {
      try db.run(table.create(ifNotExists: true) {t in
        t.column(id, primaryKey: .autoincrement)
        t.column(deviceType)
        t.column(steps)
        t.column(calorie)
        t.column(distance)
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

