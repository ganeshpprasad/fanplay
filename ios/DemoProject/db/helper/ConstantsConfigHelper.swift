//
//  ConstantsConfigHelper.swift
//  DemoProject
//
//  Created by Sysfore on 28/March/2021.
//

import UIKit
import SQLite

class ConstantsConfigHelper: DataHelperProtocol {
  static func insert(item: [ConstantsConfigModel]) throws -> Bool {
    return true
  }
  
  typealias T = ConstantsConfigModel
  static let TABLE_NAME = "ConstantsConfig"
  static let table = Table(TABLE_NAME)
  
  static func createTable() throws {
    guard let db = DTDatabase.shared?.db else{
      throw DataAccessError.Datastore_Connection_Error
    }
    do {
      try db.run(table.create(ifNotExists: true) {t in
        t.column(idStr)
        t.column(value)
      })
    } catch {
      AppConstants.log(error.localizedDescription)
      throw DataAccessError.Table_Creation_Error
    }
  }
}
