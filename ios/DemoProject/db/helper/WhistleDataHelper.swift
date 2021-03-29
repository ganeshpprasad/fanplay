//
//  WhistleDataHelper.swift
//  DemoProject
//
//  Created by Sysfore on 28/March/2021.
//

import UIKit
import SQLite

class WhistleDataHelper: DataHelperProtocol {
  
  static func insert(item: [WhistleDataModel]) throws -> Bool {
    return true
  }
  
  typealias T = WhistleDataModel
  static let TABLE_NAME = "WhistleData"
  static let table = Table(TABLE_NAME)
  
  static func createTable() throws {
    guard let db = DTDatabase.shared?.db else{
      throw DataAccessError.Datastore_Connection_Error
    }
    do {
      try db.run(table.create(ifNotExists: true) {t in
        t.column(id, primaryKey: .autoincrement)
        t.column(whistleCount)
        t.column(whistleEarned)
        t.column(whistleRedeemed)
        t.column(whistleType)
        t.column(lastUpdated)
        t.column(lastSynced)
      })
    } catch {
      AppConstants.log(error.localizedDescription)
      throw DataAccessError.Table_Creation_Error
    }
  }
}
