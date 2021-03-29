//
//  DTDatabase.swift
//  DemoProject
//
//  Created by Sysfore on 28/March/2021.
//

import UIKit
import Foundation
import SQLite
import SQLiteMigrationManager 

enum DataAccessError : Error{
  case Datastore_Connection_Error
  case Table_Creation_Error
  case Insert_Error
  case Delete_Error
  case Search_Error
  case Nil_In_Data
  case Not_Implemented
}

protocol DataHelperProtocol {
  associatedtype T
  static func createTable() throws -> Void
  static func insert(item: [T]) throws -> Bool
}

struct DTDatabase {
  let db: Connection
  let migrationManager: SQLiteMigrationManager
  
  static let shared = DTDatabase()
  
  init?() {
    do {
      self.db = try Connection(DTDatabase.storeURL().absoluteString)
    } catch {
      return nil
    }
    self.migrationManager = SQLiteMigrationManager(db: self.db, migrations: DTDatabase.migrations(), bundle: DTDatabase.migrationsBundle())
  }
  
  func createTables() throws{
    do {
      try SCDHelper.createTable()
      try DeviceHelper.createTable()
      try FanDataHelper.createTable()
      try HeartRateHelper.createTable()
      try WaveDatahelper.createTable()
      try WhistleDataHelper.createTable()
      try TeamHelper.createTable()
      try PlayersHelper.createTable()
      try AdvertiserHelper.createTable()
      try UserHelper.createTable()
      try LeaderHelper.createTable()
      try HRHelper.createTable()
      try BPHelper.createTable()
      try SleepHelper.createTable()
      try ActivityHelper.createTable()
      try SponsorHelper.createTable()
      try SponsorAnalyticsHelper.createTable()
      try UsageAnalyticsHelper.createTable()
      try ConstantsConfigHelper.createTable()
    } catch {
      AppConstants.log(error)
      throw DataAccessError.Table_Creation_Error
    }
  }
  
  func migrateIfNeeded() throws {
    if !migrationManager.hasMigrationsTable() {
      try migrationManager.createMigrationsTable()
    }
    
    if migrationManager.needsMigration() {
      try migrationManager.migrateDatabase()
    }
  }
}
extension DTDatabase {
  static func storeURL() -> URL {
    guard let documentsURL = URL(string: NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true)[0]) else {
      fatalError("could not get user documents directory URL")
    }
    return documentsURL.appendingPathComponent("bajajdt.sqlite")
  }
  
  static func migrations() -> [Migration] {
    return [ DTMigration() ]
  }
  
  static func migrationsBundle() -> Bundle {
    guard let bundleURL = Bundle.main.url(forResource: "Migrations", withExtension: "bundle") else {
      fatalError("could not find migrations bundle")
    }
    guard let bundle = Bundle(url: bundleURL) else {
      fatalError("could not load migrations bundle")
    }
    
    return bundle
  }
}

extension DTDatabase: CustomStringConvertible {
  var description: String {
    return "Database:\n" +
      "url: \(DTDatabase.storeURL().absoluteString)\n" +
      "migration state:\n" +
      "  hasMigrationsTable() \(migrationManager.hasMigrationsTable())\n" +
      "  currentVersion()     \(migrationManager.currentVersion())\n" +
      "  originVersion()      \(migrationManager.originVersion())\n" +
      "  appliedVersions()    \(migrationManager.appliedVersions())\n" +
      "  pendingMigrations()  \(migrationManager.pendingMigrations())\n" +
      "  needsMigration()     \(migrationManager.needsMigration())"
  }
}

