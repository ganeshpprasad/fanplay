package com.fanplayiot.core.db.local.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.fanplayiot.core.db.local.FanplayiotDatabase
import com.fanplayiot.core.db.local.FanplayiotTemp
import com.fanplayiot.core.db.local.dao.HomeDao
import com.fanplayiot.core.db.local.dao.HomeTempDao
import com.fanplayiot.core.db.local.entity.SponsorAnalytics
import com.fanplayiot.core.db.local.entity.SponsorData
import com.fanplayiot.core.db.local.entity.Team
import com.fanplayiot.core.db.local.entity.User
import com.fanplayiot.core.remote.repository.AdvertiserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SponsorRepository(context: Context) {
    private val dao: HomeTempDao
    private val homeDao: HomeDao
    var bannerIds: LiveData<List<Int>?>

    init {
        val tempDb = FanplayiotTemp.getTempDatabase(context)
        dao = tempDb.dao()
        bannerIds = dao.getAdvertiserIds()
        val db = FanplayiotDatabase.getDatabase(context)
        homeDao = db.homeDao()
    }

    fun addSponsors(input: Array<SponsorData>) {
        FanplayiotDatabase.databaseWriteExecutor.execute {
            try {
                if (input.isNotEmpty()) {
                    dao.deleteAllAdvertiser()
                    GlobalScope.launch(Dispatchers.IO) { dao.refresh(input) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "error ", e)
            }
        }
    }

    fun clearSponsors() {
        FanplayiotDatabase.databaseWriteExecutor.execute {
            try {
                dao.deleteAllAdvertiser()
                dao.deleteAllSponsorAnalytics()
            } catch (e: Exception) {
                Log.e(TAG, "error ", e)
            }
        }
    }

    fun refreshSponsors(advertiserRepository: AdvertiserRepository) {
        FanplayiotDatabase.databaseWriteExecutor.execute {
            try {
                var teamIdServer = 1L
                val team = homeDao.getDefaultTeam()
                if (team != null && team.teamIdServer != null) {
                    teamIdServer = team.teamIdServer!!
                }
                advertiserRepository.getSponsors(teamIdServer)
            } catch (e: Exception) {
                Log.e(TAG, "error ", e)
            }
        }
    }

    val userData: User?
        get() = homeDao.userData

    val team: Team?
        get() = homeDao.getDefaultTeam()

    suspend fun getBannerIdsForLocation(locationId: Int): List<Int>? {
        try {
            return dao.getAllAdvertiser()?.filter { sponsorData ->
                sponsorData.locationId == locationId
            }?.map { it.id }
        } catch (ex: Exception) {
            Log.e(TAG, "error ", ex)
        }
        return null
    }

    suspend fun getAdvertiser(sponsorId: Int): SponsorData? {
        return dao.getAdvertiserForId(sponsorId)
    }

    val allSponsorAnalytics: Array<SponsorAnalytics>?
        get() = dao.getAllSponsorAnalytics()

    suspend fun getSponsorAnalyticsForId(id: Int): SponsorAnalytics? {
        return dao.getSponsorAnalyticsForId(id)
    }

    suspend fun updateAdClicks(id: Int) {
        try {
            val analytics = dao.getSponsorAnalyticsForId(id) ?: return
            val newCount = analytics.noOfClicks + 1
            dao.updateSponsorAnalytics(id, SponsorAnalytics(
                    id, analytics.locationId, newCount, analytics.screenTime))
        } catch (e: Exception) {
            Log.e(TAG, "error updateAdClicks ", e)
        }

    }

    suspend fun updateScreenTime(id: Int, screenTime: Long) {
        try {
            val analytics = dao.getSponsorAnalyticsForId(id) ?: return
            var oldTime: Long = 0
            try {
                oldTime = analytics.screenTime.toLong()
            } catch (ignored: NumberFormatException) {
            }
            val newTime = oldTime + screenTime
            dao.updateSponsorAnalytics(id, SponsorAnalytics(
                    id, analytics.locationId, analytics.noOfClicks, newTime.toString()))
        } catch (e: Exception) {
            Log.e(TAG, "error updateScreenTime ", e)
        }
    }

    companion object {
        private const val TAG = "SponsorRepository"
    }
}