package com.fanplayiot.core.db.local.entity

import java.util.*

class Medical {
    private var healthIssues: HashSet<String>
    private var habits: HashSet<String>
    var bloodSugar = 0
    var bloodSugarFasting = 0
    var bloodSugarPostprandial = 0
    var fastingTime: Long = 0
    var bpSystolic = 0
    var bpDiastolic = 0
    var heartRate = 0

    constructor() {
        healthIssues = HashSet()
        habits = HashSet()
    }

    constructor(healthIssues: Set<String>, habits: Set<String>, bloodSugar: Int, bpSystolic: Int, bpDiastolic: Int, heartRate: Int) {
        this.healthIssues = HashSet()
        this.healthIssues.addAll(healthIssues)
        this.habits = HashSet()
        this.habits.addAll(habits)
        this.bloodSugar = bloodSugar
        this.bpSystolic = bpSystolic
        this.bpDiastolic = bpDiastolic
        this.heartRate = heartRate
    }

    fun getHealthIssues(): HashSet<String> {
        return healthIssues
    }

    fun setHealthIssues(healthIssues: Set<String>) {
        this.healthIssues = HashSet()
        this.healthIssues.addAll(healthIssues)
    }

    fun getHabits(): HashSet<String> {
        return habits
    }

    fun setHabits(habits: Set<String>) {
        this.habits = HashSet()
        this.habits.addAll(habits)
    }

    val bloodSugarStr: String
        get() = if (bloodSugar > 0) bloodSugar.toString() + " " + Companion.bloodSugarUnit else "100 " + Companion.bloodSugarUnit
    val bloodSugarUnit: String
        get() = Companion.bloodSugarUnit
    val bloodPressure: String
        get() = if (bpSystolic > 0 && bpDiastolic > 0) bpSystolic.toString() + "/" + bpDiastolic + " " + Companion.bloodPressureUnit else "120/80 " + Companion.bloodPressureUnit
    val bloodPressureUnit: String
        get() = Companion.bloodPressureUnit
    val heartRateUnit: String
        get() = Companion.heartRateUnit

    companion object {
        private const val bloodSugarUnit = "mg/dl"
        private const val bloodPressureUnit = "mmHg"
        private const val heartRateUnit = "bpm"
    }
}