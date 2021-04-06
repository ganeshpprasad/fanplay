package com.fanplayiot.core.db.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SponsorData(
        @PrimaryKey
        val id: Int,
        val imageUrl: String,
        val clickUrl: String,
        val locationId: Int
)

@Entity
data class SponsorAnalytics(
        @PrimaryKey
        val id: Int,
        val locationId: Int,
        val noOfClicks: Int,
        val screenTime: String
)