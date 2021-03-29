//
//  PlayersHelper.swift
//  DemoProject
//
//  Created by Sysfore on 28/March/2021.
//

import UIKit
import SQLite

class PlayersHelper: DataHelperProtocol {
  static func insert(item: [PlayersModel]) throws -> Bool {
    return true
  }
  
  typealias T = PlayersModel
  static let TABLE_NAME = "Players"
  static let table = Table(TABLE_NAME)
  
  static func createTable() throws {
    guard let db = DTDatabase.shared?.db else{
      throw DataAccessError.Datastore_Connection_Error
    }
    do {
      try db.run(table.create(ifNotExists: true) {t in
        t.column(id, primaryKey: .autoincrement)
        t.column(teamId)
        t.column(playerId) 
        t.column(playerName)
        t.column(isPlaying)
        t.column(isPlayerActive)
        t.column(tapCount)
        t.column(waveCount)
        t.column(whistleCount)
        t.column(lastUpdated)
        t.column(lastSynced)
      })
    } catch {
      AppConstants.log(error.localizedDescription)
      throw DataAccessError.Table_Creation_Error
    }
  }
}
