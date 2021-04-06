//
//  AdvertiserHelper.swift
//  DemoProject
//
//  Created by Sysfore on 28/March/2021.
//

import UIKit
import SQLite

class AdvertiserHelper: DataHelperProtocol {
  static func insert(item: [AdvertiserModel]) throws -> Bool {
    return true
  }
  
  typealias T = AdvertiserModel
  static let TABLE_NAME = "Advertiser"
  static let table = Table(TABLE_NAME)
  
  static func createTable() throws {
    guard let db = DTDatabase.shared?.db else{
      throw DataAccessError.Datastore_Connection_Error
    }
    do {
      try db.run(table.create(ifNotExists: true) {t in
        t.column(id, primaryKey: .autoincrement)
        t.column(imageUrl)
        t.column(clickUrl) 
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
