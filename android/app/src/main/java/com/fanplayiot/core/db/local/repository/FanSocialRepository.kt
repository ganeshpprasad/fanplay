package com.fanplayiot.core.db.local.repository

import android.content.Context
import android.util.Log
import com.fanplayiot.core.db.local.dao.FitnessDao
import com.fanplayiot.core.db.local.entity.*
import com.fanplayiot.core.db.local.entity.json.*
import com.fanplayiot.core.remote.repository.FanFitRepository
import com.fanplayiot.core.remote.repository.PaymentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.json.JSONObject

class FanSocialRepository(private val fitnessDao: FitnessDao, private val homeTempDao: com.fanplayiot.core.db.local.dao.HomeTempDao) {

    companion object {
        private const val TAG = "FanSocialRepository"
    }

    fun getUserSocialProfile(): Flow<UserSocialProfile?> {
        return fitnessDao.getUserSocialProfile().distinctUntilChanged()
    }

    suspend fun startFitnessActivity(start: Long, challengeId: Long, groupId: Long,
                                     affiliationId: Long, videoId: Long): Long {
        val user = fitnessDao.getUserData() ?: return -1
        if (user.sid == null) return -1
        val teamIdServer = fitnessDao.getTeamIdServer() ?: 0
        val commonJson = Json.encodeToString(FitnessChallenge(
                null, user.sid!!, teamIdServer, affiliationId, groupId,
                challengeId, videoId, LocationDetails(user.latitude, user.longitude), null))
        return fitnessDao.insert(FitnessActivity(0, 1,
                null, null, null, commonJson,
                start, 0L, 0L))
    }

    suspend fun stopFitnessActivity(context: Context, challengeName: String?, end: Long, scope: CoroutineScope): SessionSummary? {
        val id = fitnessDao.getStartedSessionId() ?: return null
        val fitnessActivity = fitnessDao.getFitnessActivityForId(id) ?: return null
        val scdList = fitnessDao.getFitnessSCDForRange(fitnessActivity.start, end)
        val scdListOfIds = scdList.map {
            it.id
        }
        val hrList = fitnessDao.getFitnessHRForRange(fitnessActivity.start, end)
        val hrListOfIds = hrList.map {
            it.id
        }
        val bpList = fitnessDao.getFitnessBPForRange(fitnessActivity.start, end)
        val bpListOfIds = bpList.map {
            it.id
        }

        if (scdListOfIds.count() + hrListOfIds.count() == 0) {
            cleanUpActivity(fitnessActivity)
            return null
        }
        val common = Json.decodeFromString<FitnessChallenge>(fitnessActivity.commonJson ?: "")
        val user = fitnessDao.getUserData() ?: return null
        common.stopSession = LocationDetails(user.latitude, user.longitude)
        fitnessDao.update(FitnessActivity(fitnessActivity.id, fitnessActivity.activityType,
                Json.encodeToString(scdListOfIds), Json.encodeToString(hrListOfIds),
                Json.encodeToString(bpListOfIds), Json.encodeToString(common),
                fitnessActivity.start, end, 0L))

        val tokenId = fitnessDao.getUserData()?.tokenId ?: return null
        val mode: Int = fitnessDao.getMode()?.toInt() ?: return null
        val fitnessRepository = FitnessRepository(context)
        val fanFitRepository = FanFitRepository(context, fitnessRepository)
        val activity = fitnessDao.getFitnessActivityForId(fitnessActivity.id) ?: return null
        fanFitRepository.postChallengeEnd(id, tokenId, context, activity, mode,
                scdList, hrList, bpList, scope)
        return getSummary(scdList, hrList, fitnessActivity.start, end, challengeName)
    }

    suspend fun getProgress(end: Long, challengeName: String?): SessionSummary? {
        val id = fitnessDao.getStartedSessionId() ?: return null
        val fitnessActivity = fitnessDao.getFitnessActivityForId(id) ?: return null
        val scdList = fitnessDao.getFitnessSCDForRange(fitnessActivity.start, end)
        val hrList = fitnessDao.getFitnessHRForRange(fitnessActivity.start, end)
        //val bpList = fitnessDao.getFitnessBPForRange(fitnessActivity.start, end)

        if (scdList.count() + hrList.count() == 0) {
            return null
        }
        return getSummary(scdList, hrList, fitnessActivity.start, end, challengeName)
    }

    private fun getSummary(scdList: List<FitnessSCD?>?, hrList: List<FitnessHR?>?,
                           start: Long, end: Long, challengeName: String?): SessionSummary? {
        if (hrList == null || hrList.isEmpty()) return null
        val steps: Int = scdList?.takeIf { it.count() > 0 }?.sumBy { it?.steps ?: 0 } ?: 0
        val calories: Double = scdList?.takeIf { it.count() > 0 }?.sumByDouble {
            it?.calories?.toDouble() ?: 0.0
        } ?: 0.0
        val distance: Double = scdList?.takeIf { it.count() > 0 }?.sumByDouble {
            it?.distance?.toDouble() ?: 0.0
        } ?: 0.0

        val totalHeartRate: Int = hrList.takeIf { it.size > 0 }?.sumBy {
            it?.heartRate ?: 0
        } ?: 0

        val avgHeartRate = totalHeartRate.takeIf { it > 0 }?.div(hrList.size) ?: 0
        var duration: Long = (end - start)
        val hour: Int = (duration / (3600 * 1000)).toInt()
        val hourStr = if (hour > 0) "$hour hr " else ""
        duration %= (3600 * 1000)
        val min: Int = (duration / (60 * 1000)).toInt()
        duration %= (60 * 1000)
        val sec: Int = duration.toInt() / 1000
        val secStr = if (sec > 0) "$sec sec" else ""
        val durationStr = "$hourStr $min min $secStr"
        return SessionSummary(
                challengeName ?: "",
                steps, calories.toInt(), distance, avgHeartRate,
                durationInMins = durationStr, duration.toInt(),
                totalHr =  totalHeartRate.toString()
        )
    }

    private suspend fun cleanUpActivity(fitnessActivity: FitnessActivity) {
        fitnessDao.delete(fitnessActivity)
    }

    fun getStartedSession(): Flow<Long?> {
        return fitnessDao.getStartedSession()
    }

    suspend fun getFitnessActivityForId(id: Long): FitnessActivity? = fitnessDao.getFitnessActivityForId(id)

    suspend fun getUserDetails(): UserOrderProfile? {
        return try {
            fitnessDao.getUserOrderProfile()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getOrderId(context: Context, key: String, name: String, desc: String,
                           challengeId: Long, amount: Double, onOrderIdCreated: (OrderDetails?) -> Unit) {
        try {
            val user = fitnessDao.getUserData() ?: return
            val userSid = fitnessDao.getUserData()?.sid ?: return
            val paymentRepository = PaymentRepository(context)
            paymentRepository.getCreateRazorpayOrder(challengeId, userSid, amount) { orderId ->
                orderId?.let {
                    onOrderIdCreated.invoke(OrderDetails(
                            orderId = orderId,
                            key = key,
                            name = name,
                            desc = desc,
                            amount = amount.toInt().toString(),
                            currency = "INR",
                            userName = user.profileName!!,
                            email = user.email,
                            contact = user.mobile,
                            address = null,
                            pincode = null,
                            sid = userSid,
                            challengeId = challengeId,
                            challengeName = "",
                            packageId = null
                    ))
                } ?: run {
                    onOrderIdCreated.invoke(null)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "error in getOrderId", e)
            onOrderIdCreated.invoke(null)
        }
    }

    suspend fun postPayment(context: Context, paymentId: String?, paymentMessage: String?,
                            onPostPayment: (String?) -> Unit) {

        try {
            val tokenId = fitnessDao.getUserData()?.tokenId ?: return
            val paymentRepository = PaymentRepository(context)
            val orderDetails: OrderDetails = homeTempDao.getMessagesForId(RAZORPAY_ORDER)?.let {
                Json.decodeFromString<OrderDetails>(it.textJson)
            } ?: return
            paymentRepository.postPaymentStatus(tokenId, orderDetails, paymentId, paymentMessage, onPostPayment)
            homeTempDao.deleteMessages(homeTempDao.getMessagesForId(RAZORPAY_ORDER))
        } catch (e: Exception) {
            Log.e(TAG, "Error in postPayment", e)
        }
    }

    suspend fun updateOrderDetails(orderDetailsStr: String) {
        try {
            homeTempDao.insertOrUpdateMessage(Messages(RAZORPAY_ORDER, System.currentTimeMillis(), orderDetailsStr))
        } catch (e: Exception) {
            Log.e(TAG, "Error in updateOrderDetails", e)
        }
    }

    suspend fun updateOrderDetailsOnCheckout(options: JSONObject) {
        try {
            val orderDetailsStr = homeTempDao.getMessagesForId(RAZORPAY_ORDER)
            orderDetailsStr?.let {
                val orderDetails = Json.decodeFromString<OrderDetails>(it.textJson)
                val prefillObj = options.optJSONObject("prefill") ?: return
                val notes = options.optJSONObject("notes")
                orderDetails.userName = prefillObj.optString("name") ?: return
                orderDetails.email = prefillObj.optString("email") ?: return
                orderDetails.contact = prefillObj.optString("contact") ?: return
                orderDetails.address = notes?.optString("address")
                orderDetails.pincode = notes?.optString("pincode")
                homeTempDao.insertOrUpdateMessage(Messages(RAZORPAY_ORDER, System.currentTimeMillis(),
                        Json.encodeToString(orderDetails)
                ))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in updateOrderDetails", e)
        }
    }
}