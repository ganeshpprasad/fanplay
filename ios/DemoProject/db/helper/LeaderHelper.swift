//
//  LeaderHelper.swift
//  DemoProject
//
//  Created by Sysfore on 28/March/2021.
//

import UIKit
import SQLite

class LeaderHelper: DataHelperProtocol {
  static func insert(item: [LeaderBoard]) throws -> Bool {
    return true
  }
  
  typealias T = LeaderBoard
  static let TABLE_NAME = "Leader"
  static let table = Table(TABLE_NAME)
  
  static func createTable() throws {
    guard let db = DTDatabase.shared?.db else{
      throw DataAccessError.Datastore_Connection_Error
    }
    do {
      try db.run(table.create(ifNotExists: true) {t in
        t.column(id, primaryKey: .autoincrement)
        t.column(rank)
        t.column(name)
        t.column(latitude)
        t.column(longitude)
        t.column(points)
        t.column(imgpath)
        t.column(avguserfanemote)
        t.column(avguserhr)
        t.column(highestuserfanemote)
        t.column(totaltapcount)
        t.column(totalwavecount)
        t.column(totalwhistleredeemed)
        t.column(highestcheeredplayer)
        t.column(lastUpdated)
        t.column(lastSynced)
      })
    } catch {
      AppConstants.log(error.localizedDescription)
      throw DataAccessError.Table_Creation_Error
    }
  }
}
