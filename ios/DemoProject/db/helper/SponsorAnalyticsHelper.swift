//
//  SponsorAnalyticsHelper.swift
//  DemoProject
//
//  Created by Sysfore on 28/March/2021.
//

import UIKit
import SQLite

class SponsorAnalyticsHelper: DataHelperProtocol {
  static func insert(item: [SponsorAnalyticsModel]) throws -> Bool {
    return true
  }
  
  typealias T = SponsorAnalyticsModel
  static let TABLE_NAME = "SponsorAnalytics"
  static let table = Table(TABLE_NAME)
  
  static func createTable() throws {
    guard let db = DTDatabase.shared?.db else{
      throw DataAccessError.Datastore_Connection_Error
    }
    do {
      try db.run(table.create(ifNotExists: true) {t in
        t.column(id, primaryKey: .autoincrement)
        t.column(locationId)
        t.column(noOfClicks)
        t.column(screenTime)
        t.column(sId)
      })
    } catch {
      AppConstants.log(error.localizedDescription)
      throw DataAccessError.Table_Creation_Error
    }
  }
}
