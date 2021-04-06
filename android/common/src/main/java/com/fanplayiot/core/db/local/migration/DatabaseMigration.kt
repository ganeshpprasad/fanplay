package com.fanplayiot.core.db.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val DB_MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
                //"CREATE TABLE IF NOT EXISTS `FitnessSCD` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `deviceType` INTEGER NOT NULL, `steps` INTEGER NOT NULL, `calories` REAL NOT NULL, `distance` REAL NOT NULL, `distanceUnit` INTEGER NOT NULL, `lastUpdated` INTEGER NOT NULL, `lastSynced` INTEGER)"
                "CREATE TABLE IF NOT EXISTS `FitnessSCD` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `deviceType` INTEGER NOT NULL, `steps` INTEGER NOT NULL, `calories` REAL NOT NULL, `distance` REAL NOT NULL, `distanceUnit` INTEGER NOT NULL, `totalSteps` INTEGER NOT NULL, `lastUpdated` INTEGER NOT NULL, `lastSynced` INTEGER)"
        )
        database.execSQL(
                "CREATE TABLE IF NOT EXISTS `FitnessHR` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `deviceType` INTEGER NOT NULL, `heartRate` INTEGER NOT NULL, `lastUpdated` INTEGER NOT NULL, `lastSynced` INTEGER)"
        )
        database.execSQL(
                "CREATE TABLE IF NOT EXISTS `FitnessBP` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `deviceType` INTEGER NOT NULL, `systolic` INTEGER NOT NULL, `diastolic` INTEGER NOT NULL, `lastUpdated` INTEGER NOT NULL, `lastSynced` INTEGER)"
        )
        database.execSQL(
                "CREATE TABLE IF NOT EXISTS `FitnessActivity` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `activityType` INTEGER NOT NULL, `activitySCD` TEXT, `activityHR` TEXT, `activityBP` TEXT, `commonJson` TEXT, `start` INTEGER NOT NULL, `end` INTEGER NOT NULL, `lastSynced` INTEGER)"
        )
        database.execSQL(
                "CREATE TABLE IF NOT EXISTS `BloodOxygen` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `deviceType` INTEGER NOT NULL, `percent` INTEGER NOT NULL, `lastUpdated` INTEGER NOT NULL, `lastSynced` INTEGER)"
        )
        database.execSQL(
                "CREATE TABLE IF NOT EXISTS `StateSteps` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `state` INTEGER NOT NULL, `stateString` TEXT NOT NULL, `lastUpdated` INTEGER NOT NULL, `lastSynced` INTEGER NOT NULL)"
        )
        database.execSQL(
                "CREATE TABLE IF NOT EXISTS `SleepData` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `sleepMinutes` INTEGER NOT NULL, `deepSleepMinutes` INTEGER NOT NULL, `lightSleepMinutes` INTEGER NOT NULL, `awakeMinutes` INTEGER NOT NULL, `restlessMinutes` INTEGER NOT NULL, `lastUpdated` INTEGER NOT NULL, `lastSynced` INTEGER NOT NULL)"
        )
        database.execSQL(
                "CREATE TABLE IF NOT EXISTS `Sedentary` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `remindMinutes` INTEGER NOT NULL, `lastUpdated` INTEGER NOT NULL, `lastSynced` INTEGER NOT NULL)"
        )
    }
}
