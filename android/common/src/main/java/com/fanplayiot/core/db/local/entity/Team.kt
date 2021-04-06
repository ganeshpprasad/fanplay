package com.fanplayiot.core.db.local.entity

import androidx.room.*

@Entity
class Team {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null

    @ColumnInfo(defaultValue = TEAM_CSK)
    var teamName: String? = null
    var teamIdServer: Long? = null

    @get:Ignore
    @set:Ignore
    @Ignore
    var teamShortName: String? = null

    @get:Ignore
    @set:Ignore
    @Ignore
    var teamPriority = 0

    @get:Ignore
    @set:Ignore
    @Ignore
    var teamLogoUrl: String? = null

    @get:Ignore
    @set:Ignore
    @Ignore
    var teamBackgroundImage: String? = null

    @get:Ignore
    @set:Ignore
    @Ignore
    var teamStoreUrl: String? = null

    @get:Ignore
    @set:Ignore
    @Ignore
    var tournamentId = 0

    @Ignore
    var tournamentName: String? = null

    constructor() {}

    @Ignore
    constructor(teamName: String?) {
        this.teamName = teamName
    }

    val rowAsString: String
        get() = "$id,$teamName"

    companion object {
        const val TEAM_CSK = "CSK"
    }
}

@Entity(foreignKeys = [ForeignKey(entity = Team::class, parentColumns = arrayOf("id"), childColumns = arrayOf("teamId"))])
class Player(
        @field:PrimaryKey
        var id: Int,

        @field:ColumnInfo(defaultValue = "1", index = true)
        var teamId: Int,

        @field:ColumnInfo(defaultValue = "1")
        var playerId: Int) {

    var playerName: String? = null
    var isPlaying = false
    var isPlayerActive = false
    var tapCount = 0
    var waveCount = 0
    var whistleCount = 0
    var url // Display image url
            : String? = null
    val rowAsString: String
        get() = ("" + id + "," + teamId + "," + url
                + "," + tapCount + "," + waveCount + "," + whistleCount)
}