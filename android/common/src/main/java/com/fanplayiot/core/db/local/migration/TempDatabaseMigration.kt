package com.fanplayiot.core.db.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val TEMP_MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
                "CREATE TABLE IF NOT EXISTS `SponsorData` (`id` INTEGER NOT NULL, `imageUrl` TEXT NOT NULL, `clickUrl` TEXT NOT NULL, `locationId` INTEGER NOT NULL, PRIMARY KEY(`id`))"
        )
        database.execSQL(
                "CREATE TABLE IF NOT EXISTS `SponsorAnalytics` (`id` INTEGER NOT NULL, `locationId` INTEGER NOT NULL, `noOfClicks` INTEGER NOT NULL, `screenTime` TEXT NOT NULL, PRIMARY KEY(`id`))"
        )
        database.execSQL(
                "CREATE TABLE IF NOT EXISTS `UsageAnalytics` (`id` TEXT NOT NULL, `type` TEXT NOT NULL, `value` INTEGER NOT NULL, PRIMARY KEY(`id`))"
        )
        database.execSQL(
                "CREATE TABLE IF NOT EXISTS `ConstantsConfig` (`id` TEXT NOT NULL, `value` INTEGER NOT NULL, PRIMARY KEY(`id`))"
        )
        database.execSQL(
                "CREATE TABLE IF NOT EXISTS `Messages` (`id` INTEGER NOT NULL, `lastSynced` INTEGER NOT NULL, `textJson` TEXT NOT NULL, PRIMARY KEY(`id`))"
        )
    }
}