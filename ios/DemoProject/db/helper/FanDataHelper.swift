//
//  FanDataHelper.swift
//  DemoProject
//
//  Created by Sysfore on 28/March/2021.
//

import UIKit
import SQLite

class FanDataHelper: DataHelperProtocol {
  static func insert(item: [FanDataModel]) throws -> Bool {
    return true
  }
  
  typealias T = FanDataModel
  static let TABLE_NAME = "FanData"
  static let table = Table(TABLE_NAME)
  
  static func createTable() throws {
    guard let db = DTDatabase.shared?.db else{
      throw DataAccessError.Datastore_Connection_Error
    }
    do {
      try db.run(table.create(ifNotExists: true) {t in
        t.column(id, primaryKey: .autoincrement)
        t.column(totalTapCount)
        t.column(fanMetric)
        t.column(totalPoints)
        t.column(flag)
        t.column(playerId)
        t.column(teamId)
        t.column(lastUpdated)
        t.column(lastSynced)
      })
    } catch {
      AppConstants.log(error.localizedDescription)
      throw DataAccessError.Table_Creation_Error
    }
  }
}
