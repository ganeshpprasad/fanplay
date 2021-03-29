//
//  DTMigration.swift
//  DemoProject
//
//  Created by Sysfore on 28/March/2021.
//

import Foundation
import SQLiteMigrationManager
import SQLite

struct DTMigration: Migration {
  var version: Int64 = 1
  
  func migrateDatabase(_ db: Connection) throws {
    
  }
}
