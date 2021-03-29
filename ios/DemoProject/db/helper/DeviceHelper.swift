//
//  DeviceHelper.swift
//  DemoProject
//
//  Created by Sysfore on 28/March/2021.
//

import UIKit
import SQLite

class DeviceHelper: DataHelperProtocol {
  
  static func insert(item: [DeviceModel]) throws -> Bool {
    return true
  }
  
  typealias T = DeviceModel
  static let TABLE_NAME = "Device"
  static let table = Table(TABLE_NAME)
  
  static func createTable() throws {
    guard let db = DTDatabase.shared?.db else{
      throw DataAccessError.Datastore_Connection_Error
    }
    do {
      try db.run(table.create(ifNotExists: true) {t in
        t.column(address)
        t.column(type)
        t.column(lastSynced)
      })
    } catch {
      AppConstants.log(error.localizedDescription)
      throw DataAccessError.Table_Creation_Error
    }
  }
}
