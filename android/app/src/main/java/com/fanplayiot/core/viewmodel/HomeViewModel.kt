package com.fanplayiot.core.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fanplayiot.core.db.local.entity.LoginType
import com.fanplayiot.core.db.local.entity.Player
import com.fanplayiot.core.db.local.entity.Team
import com.fanplayiot.core.db.local.entity.User
import com.fanplayiot.core.db.local.entity.json.SessionSummary
import com.fanplayiot.core.db.local.repository.FitnessRepository
import com.fanplayiot.core.db.local.repository.HomeRepository
import com.fanplayiot.core.db.local.repository.UserProfileStorage
import com.fanplayiot.core.remote.VolleySingleton
import com.fanplayiot.core.remote.firebase.analytics.AnalyticsService
import com.fanplayiot.core.remote.firebase.analytics.ReferralsService
import com.fanplayiot.core.remote.repository.MessageRepository
import com.fanplayiot.core.remote.repository.ProfileRepository
import com.fanplayiot.core.utils.*;
import com.fanplayiot.core.utils.OneTimeEvent
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.microsoft.windowsazure.messaging.notificationhubs.NotificationHub
import com.microsoft.windowsazure.messaging.notificationhubs.NotificationListener
import com.microsoft.windowsazure.messaging.notificationhubs.NotificationMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.*

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    @JvmField
    var playersLive: LiveData<Array<Player?>>

    @JvmField
    var fanPicture: LiveData<String?>

    @JvmField
    var storeUrl: LiveData<String?>

    @JvmField
    var userLive: LiveData<User?>

    @JvmField
    var isHRForFanFit = MutableLiveData<Boolean?>(null)

    @JvmField
    var pairPhone = MutableLiveData(false)

    @JvmField
    var imageUri = MutableLiveData<Uri?>(null)

    @JvmField
    var momentsImageUri = MutableLiveData<Uri?>(null)

    @JvmField
    var cameraResult = MutableLiveData<Boolean>(false)

    @JvmField
    var cameraResult2 = MutableLiveData<Boolean>(false)

    @JvmField
    var tabPositionLive = MutableLiveData(0)

    @JvmField
    var gotoTab = MutableLiveData(-1)

    @JvmField
    var gotoSubTab = MutableLiveData(-1)

    @JvmField
    var affStoreLink = MutableLiveData<String?>(null)

    @JvmField
    var tokenExpireTs: LiveData<OneTimeEvent<Long?>>

    @JvmField
    var teamIdServerLive: LiveData<Long?>

    var teamIdSelected: Long = -1

    private val mGotoStartCamera = MutableLiveData<OneTimeEvent<Boolean>>(null)
    val gotoStartCamera: LiveData<OneTimeEvent<Boolean>>
        get() = mGotoStartCamera
    private val mStopHeartRateReading = MutableLiveData<OneTimeEvent<Boolean>>(null)
    val stopHeartRateReading: LiveData<OneTimeEvent<Boolean>>
        get() = mStopHeartRateReading
    private val mPaymentStatus = MutableLiveData<OneTimeEvent<Boolean>>(null)
    val paymentStatus: LiveData<OneTimeEvent<Boolean>>
        get() = mPaymentStatus

    val refreshUser: LiveData<OneTimeEvent<Boolean?>>
    @JvmField
    var analyticsService = AnalyticsService

    @JvmField
    var progressLive = MutableLiveData<SessionSummary>(null)

    private val repository: HomeRepository = HomeRepository(application)
    //private val mListener: NotificationListener

    //private val mNotifications: MutableLiveData<ArrayList<NotificationMessage>> = MutableLiveData()
    private val flpClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(application)
    private val tags = listOf("fan_of_the_hour", "rewards", "normal_notifications", "event_based", "stats", "fan_emote_ind")

    init {
        /*mListener = NotificationListener { _: Context?,
                                           notificationMessage: NotificationMessage? ->
            Log.d(TAG, notificationMessage?.body + "")
        }
        NotificationHub.setListener(mListener)*/
        playersLive = repository.playersLive
        fanPicture = repository.fanPicture
        storeUrl = repository.storeUrl
        userLive = repository.userLive
        tokenExpireTs = repository.tokenExpireTs
        teamIdServerLive = repository.getTeamIdServerLive()
        refreshUser = repository.refreshUser
    }

    fun startCameraHr() {
        setGotoTabPosition(FANENGAGE_TAB_INDEX, 0)
        mGotoStartCamera.postValue(OneTimeEvent(true))
    }

    fun setStopHeartRateReading(flag: Boolean) {
        mStopHeartRateReading.postValue(OneTimeEvent(flag))
    }

    fun currentTeam(): LiveData<Team?> {
        return repository.getTeamLive()
    }

    fun updateTeamStoreUrl() {
        repository.userTeamStoreUrl
    }

    fun setTabPosition(tabPosition: Int) {
        tabPositionLive.postValue(tabPosition)
    }

    fun setGotoTabPosition(position: Int, subPosition: Int) {
        gotoTab.postValue(position)
        gotoSubTab.postValue(subPosition)
    }

    fun insertOrUpdate(tokenId: String, tokenExpires: Long, displayName: String?, @LoginType type: Int) {
        repository.insertOrUpdate(tokenId, tokenExpires, displayName, type)
    }

    fun checkTokenExpired() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Timer(TAG, false).schedule(object : TimerTask() {
                    override fun run() {
                        if (!isActive) cancel()
                        repository.getTokenIdForUpdate(FirebaseAuth.getInstance())

                    }
                }, 60 * 60000L)
            } catch (ce: CancellationException) {
                // do nothing
            } catch (e: Exception) {
                Log.e(TAG, "error", e)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun getLastLocation() {
        flpClient.lastLocation.addOnSuccessListener { location ->
            location?.run {
                repository.updateLatLong(latitude, longitude)
            }
        }
    }

    fun updateLatLong(latitude: Double, longitude: Double) {
        repository.updateLatLong(latitude, longitude)
    }

    fun inviteFriends(context: Context?) {
        logAnalytics("Home Screen Menu", "MainActivity")
        val user = userLive.value
        if (user != null) {
            if (user.profileName != null && user.sid != null) {
                val referralsService = ReferralsService()
                val dynamicLink = referralsService.createDynamicLink(user.sid!!, context!!)
                referralsService.inviteFriends(user.profileName!!, dynamicLink, context)
            }
        }
    }

    fun logAnalytics(screenName: String?, className: String?) {
        analyticsService.logScreenView(screenName!!, className!!)
    }

    fun saveFCMToken(fcmTokenInfo: String) {
        val storage = UserProfileStorage(getApplication())
        storage.fcmToken = fcmTokenInfo
    }

    fun registerUser(tokenId: String) {
        viewModelScope.launch {
            val pushNotificationRepo = repository.pushNotificationRepo
            val storage = UserProfileStorage(getApplication())


            val tagsList = mutableListOf<String>().apply {
                addAll(0, tags)
            }
            if (userLive.value == null || userLive.value!!.sid == null) return@launch
            teamIdServerLive.value?.let {
                storage.updateTeamId(it)
                val profileRepository = ProfileRepository(getApplication())
                profileRepository.postFavSportTeam(storage.getProfile(userLive.value!!))
                when (it) {
                    1L -> tagsList.add("fan_emote_csk")
                    2L -> tagsList.add("fan_emote_kbfc")
                    3L -> tagsList.add("fan_emote_ind")
                    4L -> tagsList.add("fan_emote_eng")
                    else -> {

                    }
                }
            }
            NotificationHub.addTags(tagsList)
            if (storage.fcmToken.isNotEmpty()) {
                val messageRepository = MessageRepository(VolleySingleton.getInstance(getApplication()))
                messageRepository.registerUser(tokenId, storage.fcmToken, tagsList)
                pushNotificationRepo.storeUserRegistered()
            }
            pushNotificationRepo.userRegisteredFlow.collect { config ->
                if ((config?.value ?: 0L) == 0L && storage.fcmToken.isNotEmpty()) {
                    Log.d(TAG, "user registered to azure")
                }
            }

        }
    }

    fun clearTeamsFlipperInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.masterRepository.clearTeamsFlipperInfo()
        }
    }

    fun getTeamsFlipperInfo() = repository.masterRepository.teamsFlipperLive

    fun refreshTeamsFlipperInfo(teamId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.masterRepository.getAllTeamsForSame(teamId, viewModelScope)
        }
    }

    fun onPaymentSuccess(paymentId: String?) {
        viewModelScope.launch {
            val fanSocialRepository = FitnessRepository(getApplication()).getFanSocialRepository()
            fanSocialRepository.postPayment(getApplication(), paymentId, null) {result ->
                result?.takeIf { it.equals("success", true) }.let {
                    mPaymentStatus.postValue(OneTimeEvent(true))
                }
            }
        }
    }

    fun onPaymentFailure(paymentMessage: String?) {
        viewModelScope.launch {
            val fanSocialRepository = FitnessRepository(getApplication()).getFanSocialRepository()
            fanSocialRepository.postPayment(getApplication(), null, paymentMessage) {
                // do nothing
            }
        }
    }

    fun processResponse(response: String): String {
        //  onPaymentError 0 {"error":{"code":"BAD_REQUEST_ERROR","description":"Payment processing cancelled by user","source":"customer","step":"payment_authentication","reason":"payment_cancelled"}}
        val jsonObj = JSONObject(response)
        return jsonObj.optJSONObject("error")?.optString("description") ?: "Unknown error"
    }

    companion object {
        private const val TAG = "HomeViewModel"
    }
}