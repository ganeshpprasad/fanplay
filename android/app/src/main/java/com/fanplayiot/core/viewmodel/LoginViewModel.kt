package com.fanplayiot.core.ui.auth

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fanplayiot.core.background.PlayersWork
import com.fanplayiot.core.db.local.entity.Team
import com.fanplayiot.core.db.local.entity.User
import com.fanplayiot.core.db.local.repository.HomeRepository
import com.fanplayiot.core.db.local.repository.Tournament
import com.fanplayiot.core.remote.firebase.analytics.AnalyticsService
import com.fanplayiot.core.utils.OneTimeEvent
//import com.fanplayiot.core.ui.auth.LoginActivity.*
import com.google.firebase.analytics.FirebaseAnalytics

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    val LOGIN_SCREEN = 1
    val RC_SIGN_IN = 1

    val SIGN_UP = 2
    val OTP_SCREEN = 3
    val OTP_SCREEN_SIGN_UP = 31
    val RESET_PASSWORD = 4
    val TEAM_SELECT = 5
    val MAIN_DRAWER = 10

    @JvmField
    var userLive: LiveData<User?>

    @JvmField
    var teamList: LiveData<List<Team?>>
    private val repository: HomeRepository
    var analyticsService: AnalyticsService? = null

    @JvmField
    var bundle: Bundle? = Bundle()
    val gotoScreen: MutableLiveData<OneTimeEvent<Int>> = MutableLiveData(OneTimeEvent(null))//LOGIN_SCREEN))

    val team: LiveData<Team?>
        get() = repository.getTeamLive()
    @JvmField
    var bgImageCount: MutableLiveData<Int> = MutableLiveData<Int>(0)

    init {
        repository = HomeRepository(application)
        repository.getAllTeamsInfo()
        teamList = repository.teamList
        userLive = repository.userLive
    }

    fun getAllTeamsInfo() = repository.getAllTeamsInfo()

    fun setTeam(teamName: String?, teamIdServer: Long) {
        repository.updateTeam(teamName, teamIdServer)
        PlayersWork.startPlayersSync(getApplication())
    }

    fun gotoTeamSelect() {
        if (userLive.value != null && userLive.value?.sid != null && userLive.value?.sid!! > 0) {
            gotoScreen.value = OneTimeEvent(MAIN_DRAWER)
        } else {
            gotoScreen.value = OneTimeEvent(TEAM_SELECT)
        }
    }

    fun gotoOtpScreen() {
        gotoScreen.value = OneTimeEvent(OTP_SCREEN)
    }

    fun gotoOtpScreenSignUp() {
        gotoScreen.value = OneTimeEvent(OTP_SCREEN_SIGN_UP)
    }

    fun gotoLoginScreen() {
        gotoScreen.value = OneTimeEvent(LOGIN_SCREEN)
    }

    fun gotoSignUpScreen() {
        gotoScreen.value = OneTimeEvent(SIGN_UP)
    }

    fun gotoResetPassword() {
        gotoScreen.value = OneTimeEvent(RESET_PASSWORD)
    }

    fun getTeamBgImages(): List<String> {
        return teamList.value?.sortedBy {
            it?.teamPriority
        }?.filter {
            it?.teamBackgroundImage != null && it.teamBackgroundImage!!.isNotEmpty()
                    && it.teamBackgroundImage!! != "null"
        }?.map {
            it?.teamBackgroundImage ?: ""
        } ?: emptyList()
    }

    fun getTeamsByPriority(): List<Team?> {
        return teamList.value?.sortedBy {
            it?.teamPriority
        }?.filter {
            it?.teamLogoUrl != null && it.teamLogoUrl!!.isNotEmpty() && it.teamLogoUrl!! != "null"
        } ?: listOf(Team(Team.TEAM_CSK))
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