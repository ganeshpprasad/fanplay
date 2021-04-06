package com.fanplayiot.core.db.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.fanplayiot.core.db.local.dao.HomeTempDao
import com.fanplayiot.core.db.local.migration.TEMP_MIGRATION_1_2
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Database(entities = [com.fanplayiot.core.db.local.entity.LeaderBoard::class, com.fanplayiot.core.db.local.entity.SponsorData::class, com.fanplayiot.core.db.local.entity.SponsorAnalytics::class,
    com.fanplayiot.core.db.local.entity.UsageAnalytics::class, com.fanplayiot.core.db.local.entity.ConstantsConfig::class, com.fanplayiot.core.db.local.entity.Messages::class], version = 2)
abstract class FanplayiotTemp : RoomDatabase() {
    abstract fun dao(): HomeTempDao

    companion object {
        private const val TEMP_THREADS = 1

        @JvmField
        val tempWriteExecutor: ExecutorService = Executors.newFixedThreadPool(TEMP_THREADS)

        @Volatile
        private var INSTANCE: FanplayiotTemp? = null

        @JvmStatic
        fun getTempDatabase(context: Context): FanplayiotTemp {
            synchronized(FanplayiotTemp::class.java) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.inMemoryDatabaseBuilder(context.applicationContext,
                            FanplayiotTemp::class.java)
                            .addMigrations(TEMP_MIGRATION_1_2)
                            .fallbackToDestructiveMigration().build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}