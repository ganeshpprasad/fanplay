package com.fanplayiot.core.db.local.dao

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.*
import com.fanplayiot.core.db.local.entity.*

@Dao
abstract class HomeDao {
    // Team related
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(team: Team)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract fun update(team: Team)

    @Query("SELECT * FROM Team")
    abstract fun getAllTeam(): Array<Team>

    @Query("SELECT * FROM Team WHERE teamName=:teamName")
    abstract fun getTeamForTeamName(teamName: String?): LiveData<Team?>

    @Query("SELECT * FROM Team ORDER BY id LIMIT 1 ")
    abstract fun getDefaultTeam(): Team?

    @Query("SELECT * FROM Team ORDER BY id LIMIT 1 ")
    abstract fun getTeamLive(): LiveData<Team?>

    @Query("SELECT teamIdServer FROM Team ORDER BY id LIMIT 1 ")
    abstract fun getTeamIdServerLive(): LiveData<Long?>

    // Player related
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(player: Player?)
    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract fun update(player: Player?)

    @Query("SELECT * FROM Player WHERE isPlaying = 1 AND isPlayerActive = 1 ")
    abstract fun getAllPlayer(): Array<Player>

    @Query("SELECT * FROM Player WHERE isPlaying = 1 AND isPlayerActive = 1 ")
    abstract fun getAllPlayerLive(): LiveData<Array<Player?>>

    @Query("SELECT * FROM Player WHERE playerId = :playerId AND teamId = :teamId")
    abstract fun getPlayer(playerId: Int, teamId: Int): Player?

    @Query("SELECT * FROM Player WHERE teamId = :teamId ORDER BY playerId LIMIT 1 ")
    abstract fun getDefaultPlayer(teamId: Int): Player?

    @Query("DELETE FROM Player")
    abstract fun deleteAllPlayer()

    @Transaction
    open fun refresh(list: Array<Player>) {
        deleteAllPlayer()
        for (item in list) {
            insert(item)

            /*if (item.getUrl() != null && !item.getUrl().isEmpty() && !item.getUrl().equalsIgnoreCase("null")) {
                Picasso.get().load(item.getUrl()).fetch();
            }*/
        }
    }

    private fun checkForUpdate(input: Array<Player>, players: Array<Player>?): Boolean {
        if (players == null) return true
        if (players.size == 0) return true
        if (input.size != players.size) return true
        var count = 0
        for (i in input.indices) {
            if (players[i].playerId == input[i].playerId && players[i].teamId == input[i].teamId && players[i].playerName == input[i].playerName && players[i].isPlayerActive == input[i].isPlayerActive && players[i].isPlaying == input[i].isPlaying) {
                count++
            }
        }
        return count != input.size
    }

    @Transaction
    open fun updateTeamAndPlayers(input: Array<Player>, teamName: String?, teamIdServer: Long) {
        val updatePlayers: Boolean
        val players = getAllPlayer()

        // Update Team
        val teams = getAllTeam()
        if (teams != null && teams.size > 0) {
            val currTeam = teams[0]
            currTeam.teamName = teamName
            currTeam.teamIdServer = teamIdServer
            update(currTeam)
            updatePlayers = checkForUpdate(input, players)
        } else {
            val newTeam = Team()
            newTeam.id = 1
            newTeam.teamName = teamName
            newTeam.teamIdServer = teamIdServer
            insert(newTeam)
            updatePlayers = true
        }
        if (updatePlayers) {
            Log.d(TAG, "refreshing players")
            refresh(input)
        } else {
            Log.d(TAG, "players are latest")
        }
    }

    // User related
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract fun insert(user: User?): Long
    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract fun update(user: User?)

    @get:Query("SELECT * FROM User WHERE id = 1")
    abstract val userData: User?

    @get:Query("SELECT * FROM User WHERE id = 1")
    abstract val userLive: LiveData<User?>

    @get:Query("SELECT profileImgUrl FROM User WHERE id = 1")
    abstract val imageUrl: LiveData<String?>

    @Transaction
    open fun insertOrUpdate(user: User?) {
        val wId = insert(user)
        if (wId == -1L) update(user)
    }

    // Sponsers related
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun insert(advertiser: Advertiser?): Long
    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract fun update(advertiser: Advertiser?)

    @get:Query("SELECT * FROM Advertiser")
    abstract val allAdvertiser: Array<Advertiser?>?
    @Query("SELECT * FROM Advertiser WHERE id = :sponsorId ")
    abstract fun getAdvertiserForId(sponsorId: Int): Advertiser?

    @get:Query("SELECT id FROM Advertiser")
    abstract val advertiserIds: LiveData<List<Int?>?>?
    @Query("DELETE FROM Advertiser")
    abstract fun deleteAllAdvertiser()

    @Transaction
    open fun insertOrUpdate(advertiser: Advertiser?) {
        val wId = insert(advertiser)
        if (wId == -1L) update(advertiser)
    }

    @Transaction
    open fun refresh(list: Array<Advertiser?>) {
        for (item in list) {
            insertOrUpdate(item)
        }
    }

    @Transaction
    open fun updateSponsor(id: Int, analytics: SponsorAnalyticsData) {
        val advertiser = getAdvertiserForId(id)
        if (advertiser != null) {
            advertiser.clickUrl = analytics.getJSONObject().toString()
            update(advertiser)
        }
    }

    // User history update for uninstall and install
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun insert(fanData: FanData?)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun insert(waveData: WaveData?)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun insert(whistleData: WhistleData?)

    @Transaction
    open fun updateFanEngageData(fanData: FanData?,
                            waveData: WaveData?, whistleData: WhistleData?) {
        if (fanData != null) {
            var teamId = 1
            val playerId: Int
            val team = getDefaultTeam()
            if (team?.teamIdServer != null && team.teamIdServer!! > 0) {
                teamId = team.id!!
            }
            val player = getDefaultPlayer(teamId)
            playerId = player?.playerId ?: 1
            val prevFanData = fanDataLatest
            if (prevFanData != null) {
                fanData.flag = prevFanData.flag
            }
            fanData.teamId = teamId
            fanData.playerId = playerId
            insert(fanData)
        }
        waveData?.let { insert(it) }
        whistleData?.let { insert(it) }
    }

    @Transaction
    open fun resetFanEngageData() {
        val lastUpdated = System.currentTimeMillis()
        // to insert new FanData row set teamId and playerId
        val prevFanData = fanDataLatest
        val fanData = FanData()
        if (prevFanData != null) {
            fanData.fanMetric = 0f
            fanData.totalPoints = 0L
            fanData.totalTapCount = 0
            fanData.flag = prevFanData.flag
            fanData.lastSynced = 0L
        }
        var teamId = 1
        val playerId: Int
        val team = getDefaultTeam()
        if (team != null && team.teamIdServer != null && team.teamIdServer!! > 0) {
            teamId = team.id!!
        }
        val player = getDefaultPlayer(teamId)
        playerId = player?.playerId ?: 1
        fanData.teamId = teamId
        fanData.playerId = playerId
        fanData.lastUpdated = lastUpdated
        insert(fanData)
        val waveData = WaveData()
        waveData.lastUpdated = lastUpdated
        insert(waveData)
        val whistleData = WhistleData()
        whistleData.lastUpdated = lastUpdated
        insert(whistleData)
    }

    // Leaderboard from FanData, HeartRate, WaveData and Whistle
    @get:Query("SELECT * FROM FanData ORDER BY lastUpdated DESC LIMIT 1")
    abstract val fanDataLatest: FanData?

    @get:Query("SELECT * FROM FanData ORDER BY lastUpdated DESC LIMIT 1")
    abstract val fanDataLive: LiveData<FanData?>

    @get:Query("SELECT flag FROM FanData ORDER BY lastUpdated DESC LIMIT 1")
    abstract val fanDataModeLive: LiveData<Long?>

    @get:Query("SELECT * FROM HeartRate ORDER BY lastUpdated DESC LIMIT 1")
    abstract val heartRateLatest: HeartRate?

    @get:Query("SELECT * FROM WaveData ORDER BY lastUpdated DESC LIMIT 1")
    abstract val waveDataLatest: WaveData?

    @get:Query("SELECT * FROM WaveData ORDER BY lastUpdated DESC LIMIT 1")
    abstract val waveDataLive: LiveData<WaveData?>

    @get:Query("SELECT * FROM WhistleData ORDER BY id LIMIT 1")
    abstract val whistleDataLatest: WhistleData?

    @get:Query("SELECT * FROM WhistleData ORDER BY id LIMIT 1")
    abstract val whistleDataLive: LiveData<WhistleData?>

    companion object {
        private const val TAG = "HomeDao"
    }
}