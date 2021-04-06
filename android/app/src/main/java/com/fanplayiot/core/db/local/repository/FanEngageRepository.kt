package com.fanplayiot.core.db.local.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.fanplayiot.core.db.local.FanplayiotDatabase
import com.fanplayiot.core.db.local.FanplayiotDatabase.Companion.getDatabase
import com.fanplayiot.core.db.local.dao.FanEngageDao
import com.fanplayiot.core.db.local.dao.HomeDao
import com.fanplayiot.core.db.local.dao.ScoreHelper
import com.fanplayiot.core.db.local.entity.*
import com.fanplayiot.core.remote.pojo.FanEmote
import com.fanplayiot.core.remote.repository.MainRepository
import com.fanplayiot.core.utils.Constant

class FanEngageRepository(private val context: Context) {
    private val dao: FanEngageDao
    private val homeDao: HomeDao

    @JvmField
    var band: LiveData<Device?>
    var emote: LiveData<Device?>
    var fanDataLiveData: LiveData<FanData?>
    var heartRateLiveData: LiveData<HeartRate?>
    var waveDataLiveData: LiveData<WaveData?>
    var whistleDataLiveData: LiveData<WhistleData?>
    var fansEmoteLive: LiveData<FanEmote?>
    var fanPicture: LiveData<String?>
    var modeLive: LiveData<Long?>

    // API repositories
    private val mainRepository: MainRepository
    fun deleteBand(macAddress: String) {
        FanplayiotDatabase.databaseWriteExecutor.execute {
            try {
                dao.deleteDevice(macAddress)
            } catch (e: Exception) {
                Log.e(TAG, "deleteBand DB error", e)
            }
        }
    }

    fun insertFanData(count: Int, playerId: Int) {
        val lastUpdated = System.currentTimeMillis()
        FanplayiotDatabase.databaseWriteExecutor.execute(Runnable {
            try {
                var teamId = 1
                val newPlayerId: Int
                var fanData = dao.fanDataLatest
                val team = homeDao.getDefaultTeam()
                if (team != null && team.teamIdServer!! > 0) {
                    teamId = team.id!!
                }
                var player = homeDao.getPlayer(playerId, teamId)
                if (player == null) {
                    player = homeDao.getDefaultPlayer(teamId)
                    if (player == null) {
                        Log.e(TAG, "Player not added")
                        return@Runnable
                    }
                }
                newPlayerId = if (player.playerId != null) player.playerId else 1
                if (fanData == null) {
                    fanData = FanData()
                    fanData.teamId = teamId
                    fanData.playerId = newPlayerId
                    fanData.totalTapCount = count
                    fanData.lastUpdated = lastUpdated
                    dao.insert(fanData)
                } else {
                    fanData.teamId = teamId
                    fanData.playerId = newPlayerId
                    fanData.totalTapCount = count
                    fanData.lastUpdated = lastUpdated
                    dao.update(fanData)
                }
                val tapForPlayer = player.tapCount + 1
                player.tapCount = tapForPlayer
                //Log.d(TAG, "player id " + playerId + " total tap " + count + " player tap " + tapForPlayer);
                homeDao.update(player)
            } catch (e: Exception) {
                Log.e(TAG, "DB error", e)
            }
        })
    }

    fun updateHRPref(@HeartRateType type: Int) {
        val lastUpdated = System.currentTimeMillis()
        FanplayiotDatabase.databaseWriteExecutor.execute {
            try {
                var fanData = dao.fanDataLatest //fanDataLiveData.getValue();
                if (fanData != null) {
                    fanData.flag = type.toLong()
                    dao.update(fanData)
                } else {
                    fanData = FanData()
                    fanData.flag = type.toLong()
                    fanData.lastUpdated = lastUpdated
                    dao.insert(fanData)
                }

                // Logs
                /*FanData[] list = dao.getAllFanData();
                    for (FanData item : list) {
                        Log.v(TAG, item.getRowAsString());
                    }*/
            } catch (e: Exception) {
                Log.e(TAG, "DB error", e)
            }
        }
    }

    fun insertHeartRate(heartRate: HeartRate) {
        FanplayiotDatabase.databaseWriteExecutor.execute {
            try {
                var age: Int
                if (homeDao.userData == null) {
                    age = ScoreHelper.DEFAULT_AGE
                } else {
                    age = homeDao.userData!!.age
                    if (age <= 0) age = ScoreHelper.DEFAULT_AGE
                }
                val sp = context.getSharedPreferences(Constant.PREF_FILE_KEY, Context.MODE_PRIVATE)
                val hrZone = ScoreHelper.maxHrZone(sp, age, heartRate.heartRate)
                val affiliationId = sp.getLong(Constant.AFFILIATION_KEY, 1L)
                var teamId = 1
                var playerId = 1
                val player = player
                if (player != null) {
                    playerId = if (player.playerId != null) player.playerId else 1
                    teamId = if (player.teamId != null) player.teamId else 1
                }
                val fanData = dao.insertAndCompute(heartRate, age, hrZone, teamId, playerId)
                if (fanData != null) {
                    val latestWave = dao.getWaveDataLatest()
                    val latestWhistle = dao.getWhistleDataLatest()
                    mainRepository.postFanEngagement(heartRate, hrZone, affiliationId,
                            fanData, latestWave, latestWhistle)
                }
                mainRepository.getFanEmote()
            } catch (e: Exception) {
                Log.e(TAG, "DB error", e)
            }
        }
    }

    fun getFanEmote() {
        FanplayiotDatabase.databaseWriteExecutor.execute {
            try {
                mainRepository.getFanEmote()
            } catch (e: Exception) {
                Log.e(TAG, "DB error", e)
            }
        }
    }

    fun getFanEmoteResponse(onSuccess: (String?) -> Unit) {
        try {
            mainRepository.getFanEmoteResponse(onSuccess)
        } catch (e: Exception) {
            Log.e(TAG, "getFanEmoteResponse error", e)
        }
    }

    fun getFEDetailsByTeamId(onSuccess: (String?) -> Unit) {
        try {
            mainRepository.getFEDetailsByTeamId(onSuccess)
        } catch (e: Exception) {
            Log.e(TAG, "getFEDetailsByTeamId error", e)
        }

    }
    // if available get the player

    // If fanData player is not in team selected get first player
// if not available get first player.
    // Get FanData player id, which is based on user tap
    private val player: Player?
        private get() {
            var currPlayerId = 1
            var currTeamId = 1
            val team = homeDao.getDefaultTeam()
            if (team != null && team.teamIdServer!! > 0) {
                currTeamId = team.id!!
            }
            // Get FanData player id, which is based on user tap
            val fanData = dao.fanDataLatest
            if (fanData != null) {
                currPlayerId = if (fanData.playerId != null) fanData.playerId!! else -1
            }
            var player: Player?
            if (currPlayerId == -1) {
                // if not available get first player.
                player = homeDao.getDefaultPlayer(currTeamId)
            } else {
                // if available get the player
                player = homeDao.getPlayer(currPlayerId, currTeamId)
                // If fanData player is not in team selected get first player
                if (player == null) {
                    player = homeDao.getDefaultPlayer(currTeamId)
                }
            }
            return player
        }

    /**
     * Insert wave
     *
     * @param waveData new wave data to insert
     */
    fun insertWaveData(waveData: WaveData) {
        FanplayiotDatabase.databaseWriteExecutor.execute(Runnable {
            try {
                val player = player
                if (player == null) {
                    Log.e(TAG, "Player not added")
                    return@Runnable
                }
                val temp = dao.getWaveDataLatest()
                if (temp == null) {
                    // Insert if no wave data live available as it new entry
                    dao.insert(waveData)
                } else {
                    // Update existing wave data
                    waveData.id = temp.id
                    dao.update(waveData)
                }
                val waveForPlayer = player.waveCount + 1
                player.waveCount = waveForPlayer
                homeDao.update(player)
            } catch (e: Exception) {
                Log.e(TAG, "DB error", e)
            }
        })
    }

    /**
     * insert whistle
     *
     * @param whistleData new whistle data to insert
     */
    fun insertWhistleData(whistleData: WhistleData) {
        FanplayiotDatabase.databaseWriteExecutor.execute(Runnable {
            try {
                val player = player
                if (player == null) {
                    Log.e(TAG, "Player not added")
                    return@Runnable
                }
                val temp = dao.getWhistleDataLatest()
                if (temp == null) {
                    // Insert if no value available as its new entry
                    whistleData.whistleCount = Math.abs(whistleData.whistleRedeemed - whistleData.whistleEarned)
                    dao.insert(whistleData)
                } else {
                    // Update existing whistle data
                    whistleData.id = temp.id
                    whistleData.whistleCount = Math.abs(whistleData.whistleRedeemed - whistleData.whistleEarned)
                    dao.update(whistleData)
                }
                val whistleForPlayer = player.whistleCount + 1
                player.whistleCount = whistleForPlayer
                homeDao.update(player)
            } catch (e: Exception) {
                Log.e(TAG, "DB error", e)
            }
        })
    }

    fun updatePlayerToClearCounts() {
        FanplayiotDatabase.databaseWriteExecutor.execute {
            getDatabase(context).runInTransaction {
                try {
                    val players = homeDao.getAllPlayer()
                    if (players != null && players.size > 0) {
                        for (player in players) {
                            player.tapCount = 0
                            player.waveCount = 0
                            player.whistleCount = 0
                            homeDao.update(player)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "DB error", e)
                }
            }
        }
    }

    companion object {
        private const val TAG = "FanEngageRepository"
    }

    init {
        val db = getDatabase(context)
        dao = db.dao()
        homeDao = db.homeDao()
        band = dao.getDevice(Device.DEVICE_BAND)
        emote = dao.getDevice(Device.DEVICE_EMOTE)
        fanDataLiveData = dao.getFanData()
        heartRateLiveData = dao.getHeartRate()
        waveDataLiveData = dao.getWaveData()
        whistleDataLiveData = dao.getWhistleData()
        mainRepository = MainRepository(context, homeDao, this)
        fansEmoteLive = mainRepository.fanEmoteLive
        fanPicture = homeDao.imageUrl
        modeLive = homeDao.fanDataModeLive

        // for logging
        /*FanplayiotDatabase.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                FanData[] list = dao.getAllFanData();
                for (FanData item : list) {
                    Log.v(TAG, item.getRowAsString());
                }
                HeartRate[] hrList = dao.getAllHeartRate();
                for (HeartRate item : hrList) {
                    Log.v(TAG, item.getRowAsString());
                }
                WaveData[] waveList = dao.getAllWaveData();
                for (WaveData item : waveList) {
                    Log.v(TAG, item.getRowAsString());
                }
                Device[] deviceList = dao.getAllDevice();
                Log.v(TAG, "Devices total count " + deviceList.length);
                for (Device item : deviceList) {
                    Log.v(TAG, item.getRowAsString());
                }
            }
        });*/
    }
}