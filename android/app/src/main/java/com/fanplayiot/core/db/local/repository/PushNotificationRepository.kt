package com.fanplayiot.core.db.local.repository

import com.fanplayiot.core.db.local.entity.ConstantsConfig
import kotlinx.coroutines.flow.Flow

class PushNotificationRepository(private val dao: com.fanplayiot.core.db.local.dao.HomeTempDao) {
    companion object {
        const val USER_PN_REGISTERED_AT = "USER_PN_REGISTERED_AT"
    }

    val userRegisteredFlow: Flow<ConstantsConfig?> =
        dao.getConstantsConfigForIdAsFlow(USER_PN_REGISTERED_AT)


    suspend fun storeUserRegistered() {
        dao.insertOrUpdate(ConstantsConfig(USER_PN_REGISTERED_AT, System.currentTimeMillis()), USER_PN_REGISTERED_AT)
    }
}