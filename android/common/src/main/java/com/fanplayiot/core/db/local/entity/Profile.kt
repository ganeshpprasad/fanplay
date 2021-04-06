package com.fanplayiot.core.db.local.entity

import java.util.*

class Profile {
    var user: User? = null
        private set
    var medical: Medical = Medical()
    var goal: Goal = Goal()
    var userPref: UserPref = UserPref()
    var physicalActivities: List<PhysicalActivity>

    fun setUser(user: User) {
        this.user = user
    }

    fun setPhysicalActivities(paStringSet: Set<String?>) {
        if (paStringSet.size > 0) {
            val list: MutableList<PhysicalActivity> = ArrayList()
            for (paString in paStringSet) {
                paString?.let { PhysicalActivity(it) }?.let { list.add(it) }
            }
            physicalActivities = list
        }
    }

    val stringSetPhysicalActivities: Set<String>
        get() {
            if (physicalActivities.size > 0) {
                val strPhyAct: MutableSet<String> = HashSet()
                for (pa in physicalActivities) strPhyAct.add(pa.stringValueForSelected)
                return strPhyAct
            }
            return emptySet()
        }

    init {
        physicalActivities = emptyList()
    }
}

class Goal {
    var steps: Long = 0
    var calories = 0
    var distance = 0
    var sleepTime: Long = 0
        get() {
            field = (sleepHours * 60 * 60 * 1000).toLong()
            return field
        }
        private set
    var sleepHours = 0

    constructor() {}
    constructor(steps: Long, calories: Int, distance: Int, sleepHours: Int) {
        this.steps = steps
        this.calories = calories
        this.distance = distance
        this.sleepHours = sleepHours
    }
}

class UserPref {
    var affiliationId: Long = 0
    var teamPrefId: Long = 0
    var tournamentPrefId: Long = 0
    var sportsIds = emptySet<String>()
    var isFollowFitness = false
    var isCustomFitness = false

    companion object {
        @JvmStatic
        fun getDefaultSportsIds(): Set<String> {
                val oneItemSet: MutableSet<String> = HashSet()
                oneItemSet.add(1.toString() + "")
                return oneItemSet
            }
    }
}