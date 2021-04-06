package com.fanplayiot.core.db.local.entity.json

import kotlinx.serialization.Serializable

@Serializable
data class TeamTotalInfo(
        val teamid: Long,
        val totalwave: Double = 0.0, val totalhrcount: Double = 0.0,
        val avgbpm: Double = 0.0, val totaltap: Double = 0.0,
        val teamname: String?, val teamlogourl: String?
)
