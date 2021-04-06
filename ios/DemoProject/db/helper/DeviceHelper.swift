//
//  DeviceHelper.swift
//  DemoProject
//
//  Created by Sysfore on 28/March/2021.
//

import UIKit
import SQLite

class DeviceHelper: DataHelperProtocol {
  
  static func insert(item: DeviceModel) throws -> Bool {
    guard let db = DTDatabase.shared?.db else{
      throw DataAccessError.Datastore_Connection_Error
    }
    var success = true
    
    let setter = [
      address <- item.address,
      type <- Int64(item.type),
      sId <- item.sId,
      lastSynced <- AppConstants.currentTimeInMiliseconds(),
    ]
    
    let updateUser = table.filter( sId == item.sId)
    
    do {
      let updatedRowId = try db.run(updateUser.update(setter))
      if updatedRowId > 0 {
        success = true
      }else{
        let insert = table.insert(setter)
        let insertRowId = try db.run(insert)
        success = insertRowId > 0
      }
    } catch {
      AppConstants.log(error.localizedDescription)
      throw DataAccessError.Insert_Error
    }
    return success
  }
  
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
        t.column(sId)
      })
    } catch {
      AppConstants.log(error.localizedDescription)
      throw DataAccessError.Table_Creation_Error
    }
  }
}
