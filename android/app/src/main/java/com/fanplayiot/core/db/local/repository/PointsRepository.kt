package com.fanplayiot.core.db.local.repository

import android.content.Context
import android.util.Log
import com.fanplayiot.core.db.local.FanplayiotTemp
import com.fanplayiot.core.db.local.FanplayiotTemp.Companion.getTempDatabase
import com.fanplayiot.core.db.local.entity.ConstantsConfig
import com.fanplayiot.core.remote.repository.PointsMasterRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PointsRepository(val context: Context) {
    companion object {
        private const val TAG = "PointsRepository"
        private const val REFERRAL_SID = "REFERRAL_SID"
    }

    private val tempDb = getTempDatabase(context)
    private val tempDao = tempDb.dao()

    suspend fun storeReferralSid(sidString: String?) {
        try {
            val sid = sidString?.toLong() ?: return
            tempDao.insertOrUpdate(ConstantsConfig(REFERRAL_SID, sid), REFERRAL_SID)
        } catch (e: Exception) {
            Log.e(TAG, "error in storeReferralSid ", e)
        }
    }

    fun handleReferral(receiverSid: Long, tokenId: String) {
        FanplayiotTemp.tempWriteExecutor.execute {
            try {
                val constantsConfig = tempDao.getConstantsConfigForId(REFERRAL_SID)
                constantsConfig?.takeIf { it.value != receiverSid }?.let { config ->
                    GlobalScope.launch(Dispatchers.IO) {
                        val pointsMasterRepository = PointsMasterRepository(context, this@PointsRepository)
                        pointsMasterRepository.getRnRPointsStatus(config.value, receiverSid, tokenId)
                    }

                }
            } catch (e: Exception) {
                Log.e(TAG, "error in handleReferral", e)
            }
        }
    }

    fun clearReferralSid() {
        FanplayiotTemp.tempWriteExecutor.execute {
            try {
                val constantsConfig = tempDao.getConstantsConfigForId(REFERRAL_SID)
                constantsConfig?.let { config ->
                    // Store referrer sid in Shared preference for persistence
                    val store = UserProfileStorage(context)
                    store.setReferredBySid( config.value )
                    tempDao.delete(config)
                }
            } catch (e: Exception) {
                Log.e(TAG, "error in clearReferralSid ", e)
            }
        }
    }

}