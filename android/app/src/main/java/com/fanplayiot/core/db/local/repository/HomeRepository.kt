package com.fanplayiot.core.db.local.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fanplayiot.core.db.local.FanplayiotDatabase
import com.fanplayiot.core.db.local.FanplayiotDatabase.Companion.getDatabase
import com.fanplayiot.core.db.local.FanplayiotTemp.Companion.getTempDatabase
import com.fanplayiot.core.db.local.dao.HomeDao
import com.fanplayiot.core.db.local.dao.HomeTempDao
import com.fanplayiot.core.db.local.entity.*
import com.fanplayiot.core.remote.FirebaseAuthService.currentUser
import com.fanplayiot.core.remote.FirebaseAuthService.getTokenId
import com.fanplayiot.core.remote.pojo.BaseData.Companion.getInstance
import com.fanplayiot.core.remote.pojo.TeamsInfo
import com.fanplayiot.core.remote.repository.TeamRepository
import com.fanplayiot.core.utils.Constant
import com.fanplayiot.core.utils.Helpers
import com.fanplayiot.core.utils.OneTimeEvent
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class HomeRepository(val context: Context) {

    private val teamRepository: TeamRepository
    private val pointsRepository: PointsRepository
    val masterRepository: MasterRepository
    private val dao: HomeDao
    var tempDao: HomeTempDao? = null
        private set
    val userLive: LiveData<User?>
    val fanPicture: LiveData<String?>
    var playerCount = 0
    val playersLive: LiveData<Array<Player?>>
    val fanDataLive: LiveData<FanData?>
    val waveDataLive: LiveData<WaveData?>
    val whistleDataLive: LiveData<WhistleData?>
    var teamList = MutableLiveData<List<Team?>>(null)
    var storeUrl = MutableLiveData(Constant.STORE_URL)
    var refreshUser = MutableLiveData<OneTimeEvent<Boolean?>>(null)
    var tokenExpireTs = MutableLiveData<OneTimeEvent<Long?>>(null)

    init {
        val db = getDatabase(context)
        dao = db.homeDao()
        val tempDb = getTempDatabase(context)
        tempDao = tempDb.dao()
        teamRepository = TeamRepository(context, this)
        pointsRepository = PointsRepository(context)
        masterRepository = MasterRepository(context, this, tempDao!!)
        userLive = dao.userLive
        playersLive = dao.getAllPlayerLive()
        fanPicture = dao.imageUrl
        fanDataLive = dao.fanDataLive
        waveDataLive = dao.waveDataLive
        whistleDataLive = dao.whistleDataLive
        //playersLive = new MutableLiveData<>(null);
        FanplayiotDatabase.databaseWriteExecutor.execute(Runnable { // Logging
            //User[] users = dao.getAllUser();
            //for (User user : users) Log.d(TAG, user.getRowAsString());
            try {
                val players = dao.getAllPlayer() ?: return@Runnable
                playerCount = players.size
            } catch (e: Exception) {
                Log.e(TAG, "error ", e)
            }
        })
    }

    /*constructor(context: Context, homeDao: HomeDao): this(context) {
        dao = homeDao
        init(context)
    }*/

    private fun init(context: Context) {

    }
    fun getUserTeam(): Team? = dao.getDefaultTeam()

    fun updateTeam(teamName: String?, teamIdServer: Long?) {
        FanplayiotDatabase.databaseWriteExecutor.execute {
            try {
                // Update Team
                val teams = dao.getAllTeam()
                if (teams != null && teams.size > 0) {
                    val currTeam = teams[0]
                    currTeam.teamName = teamName
                    currTeam.teamIdServer = teamIdServer
                    dao.update(currTeam)
                } else {
                    val newTeam = Team()
                    newTeam.id = 1
                    newTeam.teamName = teamName
                    newTeam.teamIdServer = teamIdServer
                    dao.insert(newTeam)
                }
            } catch (e: Exception) {
                Log.e(TAG, "update Team error ", e)
            }
        }
    }

    fun addPlayers(input: Array<Player>?, teamName: String?, teamIdServer: Long?) {
        FanplayiotDatabase.databaseWriteExecutor.execute(Runnable {
            try {
                if (input != null && input.size > 0) {

                    // Update Team
                    dao.updateTeamAndPlayers(input, teamName, teamIdServer!!)
                    val players = dao.getAllPlayer() ?: return@Runnable
                    playerCount = players.size
                }
            } catch (e: Exception) {
                Log.e(TAG, "error ", e)
            }
        })
    }

    fun initializeTeam() {
        FanplayiotDatabase.databaseWriteExecutor.execute(Runnable {
            try {
                val teams = dao.getAllTeam()
                if (teams != null && teams.size > 0) {
                    val teamId = teams[0].id!!
                    val teamName = teams[0].teamName
                    val teamIdServer = teams[0].teamIdServer
                    Log.d(TAG, "Loading team with teamId " + teamId
                            + " teamName " + teamName
                            + " team ID server " + teamIdServer)
                    // Logging
                    //Team[] tList = dao.getAllTeam();
                    //for (Team team1 : tList) Log.d(TAG, team1.getRowAsString());
                    //Player[] players = dao.getAllPlayer();
                    //playerCount = players.length;
                    //playersLive.postValue(dao.getAllPlayer());
                    //for (Player player1 : players) Log.d(TAG, player1.getRowAsString());
                } else {
                    dao.insert(Team(Team.TEAM_CSK))
                }
                val defaultTeam = dao.getDefaultTeam();
                defaultTeam?.let { teamRepository!!.getTeamAndPlayers(it) }
                val players = dao.getAllPlayer() ?: return@Runnable
                playerCount = players.size
            } catch (e: Exception) {
                Log.e(TAG, "error ", e)
            }
        })
    }

    fun insertOrUpdate(tokenId: String, tokenExpires: Long,
                       displayName: String?, @LoginType type: Int) {
        tokenExpireTs.value = OneTimeEvent(tokenExpires)
        FanplayiotDatabase.databaseWriteExecutor.execute(Runnable {
            try {
                val user = dao.userData
                if (user == null) {
                    // Call user history api
                    teamRepository!!.getRecentUserDetails(tokenId, displayName, type)
                    return@Runnable
                } else if (user.tokenId == tokenId) return@Runnable
                user.tokenId = tokenId
                if (displayName != null && !displayName.isEmpty() && user.profileName == null) {
                    user.profileName = displayName
                }
                /*val firebaseUser = FirebaseAuth.getInstance().currentUser
                if (firebaseUser != null && firebaseUser.photoUrl != null && user.profileImgUrl == null) {
                    user.profileImgUrl = firebaseUser.photoUrl.toString()
                }*/
                user.loginType = type
                val macAddress = Helpers.getMACAddress()
                if (macAddress != null) user.deviceId = macAddress
                user.phoneDeviceInfo = Helpers.getPhoneDetails()
                user.lastUpdated = System.currentTimeMillis()
                dao.insertOrUpdate(user)
                teamRepository!!.postValidateSignIn(tokenId, user.profileName, user)
            } catch (e: Exception) {
                Log.e(TAG, "error ", e)
            }
        })
    }

    suspend fun insertOrUpdateUser(tokenId: String, tokenExpires: Long,
                       displayName: String?, @LoginType type: Int) {
        tokenExpireTs.postValue(OneTimeEvent(tokenExpires))
        withContext(Dispatchers.IO) {
            try {
                val user = dao.userData
                if (user == null) {
                    // Call user history api
                    teamRepository!!.getRecentUserDetails(tokenId, displayName, type)
                    return@withContext
                } else if (user.tokenId == tokenId) return@withContext
                user.tokenId = tokenId
                if (displayName != null && !displayName.isEmpty() && user.profileName == null) {
                    user.profileName = displayName
                }
                /*val firebaseUser = FirebaseAuth.getInstance().currentUser
                if (firebaseUser != null && firebaseUser.photoUrl != null && user.profileImgUrl == null) {
                    user.profileImgUrl = firebaseUser.photoUrl.toString()
                }*/
                user.loginType = type
                val macAddress = Helpers.getMACAddress()
                if (macAddress != null) user.deviceId = macAddress
                user.phoneDeviceInfo = Helpers.getPhoneDetails()
                user.lastUpdated = System.currentTimeMillis()
                dao.insertOrUpdate(user)
                //teamRepository!!.postValidateSignIn(tokenId, user.profileName, user)
            } catch (e: Exception) {
                Log.e(TAG, "error ", e)
            }
        }
    }

    fun getTokenIdForUpdate(firebaseAuth: FirebaseAuth) {
        getTokenId(firebaseAuth, this)
    }

    fun updateTokenId(tokenId: String, expires: Long) {
        tokenExpireTs.value = OneTimeEvent(expires)
        FanplayiotDatabase.databaseWriteExecutor.execute(Runnable {
            try {
                val user = dao.userData
                if (user == null) {
                    // User cannot be null
                    return@Runnable
                } else if (user.tokenId == tokenId) return@Runnable
                user.tokenId = tokenId
                user.lastUpdated = System.currentTimeMillis()
                dao.insertOrUpdate(user)
                Log.d(TAG, "user tokenId updated")
                teamRepository!!.postValidateSignIn(tokenId, user.profileName, user)
            } catch (e: Exception) {
                Log.e(TAG, "error ", e)
            }
        })
    }

    fun getNewUserDetails(tokenId: String, displayName: String?, @LoginType type: Int): User {
        val user = User()
        if (displayName != null && !displayName.isEmpty()) {
            user.profileName = displayName
        }
        /*val firebaseUser = currentUser()
        if (firebaseUser != null) {
            if (firebaseUser.email != null) {
                user.email = firebaseUser.email
            }
            if (firebaseUser.phoneNumber != null) {
                user.mobile = firebaseUser.phoneNumber
            }
            if (firebaseUser.photoUrl != null) {
                user.profileImgUrl = firebaseUser.photoUrl.toString()
            }
        }*/
        user.timeZone = GregorianCalendar.getInstance().timeZone.displayName
        user.tokenId = tokenId
        user.loginType = type
        val macAddress = Helpers.getMACAddress()
        if (macAddress != null) user.deviceId = macAddress
        user.phoneDeviceInfo = Helpers.getPhoneDetails()
        user.lastUpdated = System.currentTimeMillis()
        return user
    }

    fun updateUser(userSid: Long?) {
        FanplayiotDatabase.databaseWriteExecutor.execute(Runnable {
            try {
                val user = dao.userData
                if (user == null) {
                    Log.e(TAG, "user is null")
                    return@Runnable
                }
                user.sid = userSid
                user.lastUpdated = System.currentTimeMillis()
                dao.update(user)
                // New user so check for referrals if applicable
                pointsRepository!!.handleReferral(userSid!!, user.tokenId!!)
                refreshUser.postValue(OneTimeEvent<Boolean>(true))
            } catch (e: Exception) {
                Log.e(TAG, "error ", e)
            }
        })
    }

    val userData: User?
        get() = dao.userData

    fun updateLatLong(latitude: Double, longitude: Double) {
        FanplayiotDatabase.databaseWriteExecutor.execute(Runnable {
            try {
                val user = dao.userData ?: return@Runnable
                user.latitude = latitude
                user.longitude = longitude
                user.lastUpdated = System.currentTimeMillis()
                dao.update(user)
                teamRepository!!.postValidateSignIn(user.tokenId!!, user.profileName, user)
            } catch (e: Exception) {
                Log.e(TAG, "error ", e)
            }
        })
    }

    fun insertUserRecent(user: User) {
        FanplayiotDatabase.databaseWriteExecutor.execute {
            try {
                dao.insertOrUpdate(user)
                getFanEngageData()
                teamRepository!!.postValidateSignIn(user.tokenId!!, user.profileName, user)
            } catch (e: Exception) {
                Log.e(TAG, "error ", e)
            }
        }
    }

    fun getAllTeamsInfo() {
            FanplayiotDatabase.databaseWriteExecutor.execute {
                try {
                    val messages = tempDao!!.getMessageForId(GET_ALL_TEAMS_ID)
                    if (messages != null) {
                        val teamsInfo: TeamsInfo? = getInstance<TeamsInfo>(TeamsInfo::class.java, messages.textJson)
                        if (teamsInfo != null && teamsInfo.teams != null) {
                            teamList.postValue(teamsInfo.teams)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "error ", e)
                }
            }
            teamRepository!!.getAllTeamsInfo()
        }

    // Get user current team
    fun getTeamLive() = dao.getTeamLive()

    fun getTeamIdServerLive() = dao.getTeamIdServerLive()

    // Get All Teams info

    // Get user current team store url
    val userTeamStoreUrl: Unit
        get() {
            FanplayiotDatabase.databaseWriteExecutor.execute(Runnable {
                try {
                    val currentMessage = tempDao!!.getMessageForId(GET_ALL_TEAMS_ID)
                    if (currentMessage != null) {
                        // Get user current team
                        val team: Team? = dao.getDefaultTeam()
                        val teamIdServer = if (team != null && team.teamIdServer != null) team.teamIdServer!! else 1L

                        // Get All Teams info
                        val teamsInfo: TeamsInfo? = getInstance<TeamsInfo>(TeamsInfo::class.java, currentMessage.textJson)
                        if (teamsInfo != null && teamsInfo.teams != null) {
                            val list = teamsInfo.teams
                            for (item in list!!) {
                                if (item.teamIdServer == teamIdServer) {
                                    val url = item.teamStoreUrl
                                    storeUrl.postValue(if (url != null && !url.isEmpty()) url else Constant.STORE_URL)
                                    return@Runnable
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "error in setTeamsList ", e)
                }
            })
        }

    fun setTeamsList(jsonString: String, teamsJsonString: String, teams: List<Team>) {
        FanplayiotDatabase.databaseWriteExecutor.execute {
            try {
                val currentMessage = tempDao!!.getMessageForId(GET_ALL_TEAMS_ID)
                if (currentMessage != null) {
                    val teamsInfo: TeamsInfo? = getInstance<TeamsInfo>(TeamsInfo::class.java, currentMessage.textJson)
                    if (teamsInfo != null && teamsInfo.jsonString != null) {
                        if (!teamsInfo.jsonString.equals(teamsJsonString, ignoreCase = true)) {
                            tempDao!!.replaceMessage(currentMessage, Messages(
                                    GET_ALL_TEAMS_ID, System.currentTimeMillis(), jsonString))
                            teamList.postValue(teams)
                        }
                    } else {
                        tempDao!!.replaceMessage(currentMessage, Messages(
                                GET_ALL_TEAMS_ID, System.currentTimeMillis(), jsonString))
                        teamList.postValue(teams)
                    }
                } else {
                    tempDao!!.insert(Messages(
                            GET_ALL_TEAMS_ID, System.currentTimeMillis(), jsonString))
                    teamList.postValue(teams)
                }
            } catch (e: Exception) {
                Log.e(TAG, "error in setTeamsList ", e)
            }
        }
    }

    fun getFanEngageData() {
            FanplayiotDatabase.databaseWriteExecutor.execute(Runnable {
                try {
                    val user = dao.userData ?: return@Runnable
                    if (user.tokenId!!.isEmpty()) {
                        return@Runnable
                    }
                    val team: Team = dao.getDefaultTeam() ?: return@Runnable
                    if (team.teamIdServer == null) {
                        return@Runnable
                    }
                    teamRepository!!.getFanEngageDataForTeam(user.tokenId!!, team.teamIdServer!!)
                } catch (e: Exception) {
                    Log.e(TAG, "error in updateFanEngageData ", e)
                }
            })
        }

    /**
     * Called from TeamRepository getFanEngageDataForTeam() method when tap, wave, whistle values
     * are available for the logged in user and team selected.
     * @param fanData FanData
     * @param waveData WaveData
     * @param whistleData WhistleData
     */
    fun updateFanEngageData(fanData: FanData?,
                            waveData: WaveData?, whistleData: WhistleData?) {
        FanplayiotDatabase.databaseWriteExecutor.execute {
            try {
                dao.updateFanEngageData(fanData, waveData, whistleData)
            } catch (e: Exception) {
                Log.e(TAG, "error in updateFanEngageData ", e)
            }
        }
    }

    /**
     * Called from TeamRepository getFanEngageDataForTeam() method when no values are available
     * for the logged in user and team selected. Also called when there are other error conditions
     *
     */
    fun resetFanEngageData() {
        FanplayiotDatabase.databaseWriteExecutor.execute {
            try {
                dao.resetFanEngageData()
            } catch (e: Exception) {
                Log.e(TAG, "error in resetFanEngageData ", e)
            }
        }
    }

    val pushNotificationRepo: PushNotificationRepository
        get() = PushNotificationRepository(tempDao!!)

    companion object {
        private const val TAG = "HomeRepository"
    }
}