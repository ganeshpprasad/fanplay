package com.fanplayiot.core.db.local.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.fanplayiot.core.db.local.dao.HomeTempDao
import com.fanplayiot.core.db.local.entity.*
import com.fanplayiot.core.db.local.entity.json.TeamTotalInfo
import com.fanplayiot.core.remote.pojo.BaseData.Companion.getInstance
import com.fanplayiot.core.remote.pojo.TeamsInfo
import com.fanplayiot.core.remote.repository.MasterRemoteRepository
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.math.round

sealed class TeamsFlipper {
    data class TeamFlipperInfo(val leftTeamName: String,
                               val leftTeamLogo: String,
                               val leftTeamInfo: String,
                               val rightTeamName: String,
                               val rightTeamLogo: String,
                               val rightTeamInfo: String,
                               var infoType: Int) : TeamsFlipper()
    object HideView : TeamsFlipper()
}

data class Tournament(var tournamentId: Int, var name: String, var teamParticipate: List<Team>)

class MasterRepository(context: Context,
                       private val homeRepository: HomeRepository,
                       private val dao: com.fanplayiot.core.db.local.dao.HomeTempDao) {

    val teamsFlipperLive = MutableLiveData<TeamsFlipper>(TeamsFlipper.HideView)
    private val masterRemoteRepository = MasterRemoteRepository(context, this@MasterRepository)

    companion object {
        private const val TAG = "MasterRepository"
    }

    fun getAllTeamsSync() {
        homeRepository.getAllTeamsInfo()
    }

    fun getAllTournaments(allTeams: List<Team>?) : List<Tournament>  {
        val tourList = allTeams?.map {
            Tournament(it.tournamentId, it.tournamentName ?: "", emptyList())
        }?.distinct() ?: emptyList()

        tourList.forEach { tour ->
            val teamPar = allTeams?.filter {
                it.tournamentId == tour.tournamentId
            } ?: emptyList()
            tour.teamParticipate = teamPar
        }

        return tourList
    }

    suspend fun getAllTeamsForSame(teamId: Long, scope: CoroutineScope) {
        try {
            val json = dao.getMessagesForId(com.fanplayiot.core.db.local.entity.GET_ALL_TEAMS_ID)?.textJson
            json?.run {
                val allList = getInstance(TeamsInfo::class.java, this)?.teams

                // Filter all teams in a tournament which the user selected team is present
                val currTourId = allList?.find {
                    it.teamIdServer == teamId
                }?.tournamentId ?: -1
                if (currTourId > 0) {

                    // Subset of teams in same tournament select only if 2 teams are present
                    val subTeamList: List<Team> = allList?.filter {
                        it.tournamentId == currTourId
                    }?.takeIf {
                        it.isNotEmpty() && it.size == 2
                    } ?: emptyList()

                    // If not teams available return
                    if (subTeamList.isEmpty()) return
                    val leftTeam = if (subTeamList[0].teamIdServer == teamId) subTeamList[0] else subTeamList[1]
                    val rightTeam = if (subTeamList[0].teamIdServer == teamId) subTeamList[1] else subTeamList[0]
                    val teamTotalInfoList = dao.getMessagesForId(com.fanplayiot.core.db.local.entity.GET_FE_DETAILS_BY_TEAMS_ID)?.textJson?.let {
                        Json.decodeFromString<List<TeamTotalInfo>>(it)
                    }
                    if (teamTotalInfoList.isNullOrEmpty()) {
                        teamsFlipperLive.postValue(
                                TeamsFlipper.TeamFlipperInfo(
                                        leftTeam.teamName ?: "",
                                        leftTeam.teamLogoUrl ?: "",
                                        "0",
                                        rightTeam.teamName ?: "",
                                        rightTeam.teamLogoUrl ?: "",
                                        "0",
                                        com.fanplayiot.core.db.local.entity.INFO_TYPE_HR
                                )
                        )
                    } else {
                        val leftInfo = if (teamTotalInfoList[0].teamid == teamId)  teamTotalInfoList[0] else teamTotalInfoList[1]
                        val rightInfo = if (teamTotalInfoList[0].teamid == teamId)  teamTotalInfoList[1] else teamTotalInfoList[0]

                        teamsFlipperLive.postValue(
                                TeamsFlipper.TeamFlipperInfo(
                                        leftInfo.teamname ?: "",
                                        leftInfo.teamlogourl ?: "",
                                        getTextForValue(leftInfo.totalhrcount),
                                        rightInfo.teamname ?: "",
                                        rightInfo.teamlogourl ?: "",
                                        getTextForValue(rightInfo.totalhrcount),
                                        com.fanplayiot.core.db.local.entity.INFO_TYPE_HR
                                )
                        )
                    }



                    if (subTeamList[0].teamIdServer != null &&
                            subTeamList[1].teamIdServer != null &&
                            subTeamList[0].teamIdServer == teamId) {
                        getTotalsForTeam(subTeamList[0].teamIdServer!!, subTeamList[1].teamIdServer!!, scope)
                    } else {
                        getTotalsForTeam(subTeamList[1].teamIdServer!!, subTeamList[0].teamIdServer!!, scope)
                    }
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "error in getAllTeamsForSame", e)
        }
    }

    suspend fun clearTeamsFlipperInfo() {
        try {
            teamsFlipperLive.postValue(TeamsFlipper.HideView)
            dao.getMessagesForId(com.fanplayiot.core.db.local.entity.GET_FE_DETAILS_BY_TEAMS_ID)?.let { dao.delete(it) }
        } catch (e: Exception) {
            Log.d(TAG, "error in clearTeamsFlipperInfo", e)
        }
    }

    private fun getTextForValue(value: Double) : String {
        return if (value > 1000) round(value / 1000).toLong().toString() + "K"
        else round(value).toLong().toString()
    }

    private fun getTotalsForTeam(leftTeamId: Long, rightTeamId: Long, scope: CoroutineScope) {
        masterRemoteRepository.apply {
            getTotalsTeam(leftTeamId, rightTeamId, scope)
        }
    }

    fun insertMaster(input: com.fanplayiot.core.db.local.entity.Messages, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            dao.insertOrUpdateMessage(input)
        }
    }


}