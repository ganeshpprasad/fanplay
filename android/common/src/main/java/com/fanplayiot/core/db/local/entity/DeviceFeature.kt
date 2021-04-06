package com.fanplayiot.core.db.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class StateSteps(
        @PrimaryKey(autoGenerate = true)
        val id: Long,
        val state: Int,
        val stateString: String,
        val lastUpdated: Long,
        val lastSynced: Long
)

@Entity
data class SleepData(
        @PrimaryKey(autoGenerate = true)
        val id: Long,
        val sleepMinutes: Int,
        val deepSleepMinutes: Int,
        val lightSleepMinutes: Int,
        val awakeMinutes: Int,
        val restlessMinutes: Int,
        val lastUpdated: Long,
        val lastSynced: Long
)

@Entity
data class Sedentary(
        @PrimaryKey(autoGenerate = true)
        val id: Long,
        val remindMinutes: Int,
        val lastUpdated: Long,
        val lastSynced: Long
)
