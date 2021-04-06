package com.fanplayiot.core.db.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FitnessActivity(
        @PrimaryKey(autoGenerate = true)
        val id: Long,
        val activityType: Int,
        val activitySCD: String?,
        val activityHR: String?,
        val activityBP: String?,
        var commonJson: String?,
        val start: Long,
        val end: Long,
        var lastSynced: Long?
)