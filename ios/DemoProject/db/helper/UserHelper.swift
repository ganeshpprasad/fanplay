//
//  UserHelper.swift
//  DemoProject
//
//  Created by Sysfore on 28/March/2021.
//

import UIKit
import SQLite

class UserHelper: DataHelperProtocol {
  static func insert(item: [UserModel]) throws -> Bool {
    return true
  }
  
  typealias T = UserModel
  static let TABLE_NAME = "User"
  static let table = Table(TABLE_NAME)
  
  static func createTable() throws {
    guard let db = DTDatabase.shared?.db else{
      throw DataAccessError.Datastore_Connection_Error
    }
    do {
      try db.run(table.create(ifNotExists: true) {t in
        t.column(id, primaryKey: .autoincrement)
        t.column(tokenId)
        t.column(sid)
        t.column(latitude)
        t.column(longitude)
        t.column(timeZone)
        t.column(age)
        t.column(loginType)
        t.column(profileName)
        t.column(profileImgUrl)
        t.column(teamPref)
        t.column(gender)
        t.column(mobile)
        t.column(email)
        t.column(dob)
        t.column(city)
        t.column(height)
        t.column(heightMeasure)
        t.column(weightMeasure)
        t.column(deviceId)
        t.column(phoneDeviceInfo)
        t.column(lastUpdated)
        t.column(lastSynced)
      })
    } catch {
      AppConstants.log(error.localizedDescription)
      throw DataAccessError.Table_Creation_Error
    }
  }
}
