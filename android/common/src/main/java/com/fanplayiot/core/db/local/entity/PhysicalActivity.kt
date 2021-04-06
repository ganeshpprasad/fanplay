package com.fanplayiot.core.db.local.entity

class PhysicalActivity {
    enum class Grade(var value: Int) {
        LIGHT(1), MODERATE(2), VIGOROUS(3);
    }

    var pid = 0
    var activityName: String? = null
    private var period // Physical Activity time period in minutes : total value for a week (7 days);
            : Long = 0
    var perDayHour = 0
    var perDayMin = 0
    var perWeekHour = 0
    var perWeekMin = 0

    constructor(pid: Int, activityName: String?) {
        this.pid = pid
        this.activityName = activityName
    }

    constructor(stringValueSelected: String) {
        val temp = stringValueSelected.split(SPLIT_CHAR.toRegex()).toTypedArray()
        require(temp.size == 6) { "Not enough data for Physical Activity" }
        try {
            pid = temp[0].toInt()
            activityName = temp[1]
            perDayHour = temp[2].toInt()
            perDayMin = temp[3].toInt()
            perWeekHour = temp[4].toInt()
            perWeekMin = temp[5].toInt()
        } catch (ignore: NumberFormatException) {
        }
    }

    fun getPeriod(): Long {
        period = ((perDayHour * 60 + perDayMin) * 7).toLong()
        if (period == 0L) period = (perWeekHour * 60 + perWeekMin).toLong()
        return period
    }

    val grade: Grade
        get() {
            period = ((perDayHour * 60 + perDayMin) * 7).toLong()
            if (period == 0L) period = (perWeekHour * 60 + perWeekMin).toLong()
            return if (period <= 150) Grade.LIGHT else if (period > 150 && period <= 360) Grade.MODERATE else Grade.VIGOROUS
        }
    val stringValueForSelected: String
        get() = pid.toString() + SPLIT_CHAR + activityName +
                SPLIT_CHAR + perDayHour +
                SPLIT_CHAR + perDayMin +
                SPLIT_CHAR + perWeekHour +
                SPLIT_CHAR + perWeekMin

    companion object {
        const val SPLIT_CHAR = "#"
    }
}