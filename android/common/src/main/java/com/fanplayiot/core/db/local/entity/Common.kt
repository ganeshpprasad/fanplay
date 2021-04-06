package com.fanplayiot.core.db.local.entity

import androidx.annotation.IntDef
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UsageAnalytics(
        @PrimaryKey
        val id: String,
        val type: String,
        val value: Long
)

@Entity
data class ConstantsConfig(
        @PrimaryKey
        val id: String,
        val value: Long
)

@Entity
data class Messages(
        @PrimaryKey
        var id: Int,
        var lastSynced: Long,
        var textJson: String,
)

// ID for Messages used for caching API response
const val GET_ALL_TEAMS_ID = 1
const val GET_FE_DETAILS_BY_TEAMS_ID = 2
const val RAZORPAY_ORDER = 3

const val INFO_TYPE_HR = 1
const val INFO_TYPE_TAP = 2
const val INFO_TYPE_WAVE = 3
const val INFO_TYPE_WH = 4

@Retention(AnnotationRetention.SOURCE)
@IntDef(INFO_TYPE_HR, INFO_TYPE_TAP, INFO_TYPE_WAVE, INFO_TYPE_WH)
annotation class TeamInfoType