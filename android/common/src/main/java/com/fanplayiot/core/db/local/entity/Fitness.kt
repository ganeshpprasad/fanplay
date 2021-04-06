package com.fanplayiot.core.db.local.entity

import androidx.annotation.IntDef
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

const val DEVICE_BAND = 1
const val GOOGLE_FIT = 2
const val PHONE = 3
const val FIT_BIT = 4
const val DISTANCE_UNIT_KM = 1
const val DISTANCE_UNIT_MILES = 2
const val GROUP_BY_HOURS = 1
const val GROUP_BY_WEEK = 2
const val GROUP_BY_MONTH = 3

@kotlin.annotation.Retention(AnnotationRetention.SOURCE)
@IntDef(DISTANCE_UNIT_KM, DISTANCE_UNIT_MILES)
annotation class DistanceUnit

@kotlin.annotation.Retention(AnnotationRetention.SOURCE)
@IntDef(DEVICE_BAND, GOOGLE_FIT, PHONE, FIT_BIT)
annotation class FitnessDeviceType

@Entity
data class FitnessSCD(
        @PrimaryKey(autoGenerate = true)
        val id: Long,
        val deviceType: Int,
        val steps: Int,
        val calories: Float,
        val distance: Float,
        @DistanceUnit
        val distanceUnit: Int,
        val totalSteps: Int = 0,
        val lastUpdated: Long,
        var lastSynced: Long?
) {
        @Ignore
        constructor(steps: Int, calories: Float, distance: Float) : this(0, DEVICE_BAND, steps, calories, distance, DISTANCE_UNIT_KM, 0, System.currentTimeMillis(), 0)

        @Ignore
        constructor(steps: Int, totalSteps: Int, calories: Float, distance: Float, lastUpdated: Long) : this(0, DEVICE_BAND, steps, calories, distance, DISTANCE_UNIT_KM, totalSteps, lastUpdated, 0)
}

@Entity
data class FitnessHR(
        @PrimaryKey(autoGenerate = true)
        val id: Long,
        val deviceType: Int,
        val heartRate: Int,
        val lastUpdated: Long,
        var lastSynced: Long?
) {
        @Ignore
        constructor(heartRate: Int) : this(0, DEVICE_BAND, heartRate, System.currentTimeMillis(), 0)

        @Ignore
        constructor(heartRate: Int, lastUpdated: Long) : this(0, DEVICE_BAND, heartRate, lastUpdated, 0)
}

@Entity
data class FitnessBP(
        @PrimaryKey(autoGenerate = true)
        val id: Long,
        val deviceType: Int,
        val systolic: Int,
        val diastolic: Int,
        val lastUpdated: Long,
        var lastSynced: Long?
) {
        @Ignore
        constructor(systolic: Int, diastolic: Int) : this(0, DEVICE_BAND, systolic, diastolic, System.currentTimeMillis(), 0)

        @Ignore
        constructor(systolic: Int, diastolic: Int, lastUpdated: Long) : this(0, DEVICE_BAND, systolic, diastolic, lastUpdated, 0)
}

@Entity
data class BloodOxygen(
        @PrimaryKey(autoGenerate = true)
        val id: Long,
        val deviceType: Int,
        val percent: Int,
        val lastUpdated: Long,
        var lastSynced: Long?
) {
        @Ignore
        constructor(percent: Int) : this(0, DEVICE_BAND, percent, System.currentTimeMillis(), 0)
}

data class StepDuration(
        var steps: Int,
        var calories: Float,
        var distance: Float,
        var duration: Long
)

data class HrDuration(
        var heartRate: Int,
        var duration: Long
)

data class BpDuration(
        var systolic: Int,
        var diastolic: Int,
        var duration: Long
)

data class SessionFitnessIds(
        var activitySCD: String?, var activityHR: String?, var activityBP: String?
)