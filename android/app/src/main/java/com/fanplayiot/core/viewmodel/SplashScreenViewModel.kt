package com.fanplayiot.core.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.fanplayiot.core.background.LeaderBoardWork
import com.fanplayiot.core.background.PlayersWork
import com.fanplayiot.core.background.WorkerHelper
import com.fanplayiot.core.db.local.entity.Team
import com.fanplayiot.core.db.local.repository.HomeRepository
import com.fanplayiot.core.db.local.repository.PointsRepository
import com.fanplayiot.core.remote.firebase.analytics.AnalyticsService
import com.fanplayiot.core.remote.firebase.analytics.ReferralsService
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class SplashScreenViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: HomeRepository
    private val pointsRepository: PointsRepository
    private val playerWorkUUID: UUID
    private val lbWorkUUID: UUID
    private val playerWorkInfo: LiveData<List<WorkInfo>>
    private val lbWorkInfo: LiveData<List<WorkInfo>>
    // Referrals Service
    val referralsService = ReferralsService()
    var analyticsService: AnalyticsService? = null

    init {
        repository = HomeRepository(application)
        pointsRepository = PointsRepository(application)
        repository.getAllTeamsInfo()
        playerWorkUUID = PlayersWork.startPlayersSync(application)
        lbWorkUUID = LeaderBoardWork.startLeaderBoardWork(application)
        playerWorkInfo = WorkerHelper.isWorkStarted(application, PlayersWork.TAG)
        lbWorkInfo = WorkerHelper.isWorkStarted(application, LeaderBoardWork.TAG)
    }

    @JvmField
    var appUpdateChecked = false
    @JvmField
    var isInternetAvailable = false
    @JvmField
    var animationPlayed = false

    fun currentTeam(): LiveData<Team?> {
        return repository.getTeamLive()
    }

    fun allTeams(): LiveData<List<Team?>> {
        return repository.teamList
    }

    fun checkTeamsInfo() {
        repository.getAllTeamsInfo()
    }

    fun storeReferrerSid(sid: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            pointsRepository.storeReferralSid(sid)
        }
    }

    fun setFirebaseAnalytics(faInstance: FirebaseAnalytics) {
        // Firebase Analytics
        analyticsService = AnalyticsService
        analyticsService?.init(faInstance)
    }

    fun logScreenView(screenName: String, className: String) {
        analyticsService?.logScreenView(screenName, className)
    }
}