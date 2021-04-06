package com.fanplayiot.core.db.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.fanplayiot.core.db.local.dao.FanEngageDao
import com.fanplayiot.core.db.local.dao.FitnessDao
import com.fanplayiot.core.db.local.dao.HomeDao
import com.fanplayiot.core.db.local.migration.DB_MIGRATION_1_2
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Database(entities = [com.fanplayiot.core.db.local.entity.Advertiser::class, com.fanplayiot.core.db.local.entity.Device::class, com.fanplayiot.core.db.local.entity.FanData::class, com.fanplayiot.core.db.local.entity.HeartRate::class,
    com.fanplayiot.core.db.local.entity.Player::class, com.fanplayiot.core.db.local.entity.Team::class, com.fanplayiot.core.db.local.entity.User::class, com.fanplayiot.core.db.local.entity.WaveData::class, com.fanplayiot.core.db.local.entity.WhistleData::class,
    com.fanplayiot.core.db.local.entity.FitnessSCD::class, com.fanplayiot.core.db.local.entity.FitnessHR::class, com.fanplayiot.core.db.local.entity.FitnessBP::class, com.fanplayiot.core.db.local.entity.FitnessActivity::class,
    com.fanplayiot.core.db.local.entity.BloodOxygen::class, com.fanplayiot.core.db.local.entity.StateSteps::class, com.fanplayiot.core.db.local.entity.SleepData::class, com.fanplayiot.core.db.local.entity.Sedentary::class], version = 2)
abstract class FanplayiotDatabase : RoomDatabase() {
    abstract fun dao(): FanEngageDao
    abstract fun homeDao(): HomeDao
    abstract fun fitnessDao(): FitnessDao

    companion object {
        @Volatile
        private var INSTANCE: FanplayiotDatabase? = null
        private const val DB_THREADS = 4

        @JvmField
        val databaseWriteExecutor: ExecutorService = Executors.newFixedThreadPool(DB_THREADS)

        @JvmStatic
        fun getDatabase(context: Context): FanplayiotDatabase {
            synchronized(FanplayiotDatabase::class.java) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(context.applicationContext,
                            FanplayiotDatabase::class.java, "fanplay_db")
                            .addMigrations(DB_MIGRATION_1_2)
                            .build()
                    INSTANCE = instance
                }
                return instance
            }

        }
    }
}