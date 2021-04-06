package com.fanplayiot.core.db.local.entity.json

import kotlinx.serialization.Serializable

@Serializable
data class FitnessChallenge(
        var sessionId: Long?,
        var sid: Long,
        var teamIdServer: Long,
        var affiliationId: Long?,
        var groupId: Long?,
        var challengeId: Long,
        var videoId: Long,

        var startSession: LocationDetails?,
        var stopSession: LocationDetails?
)

@Serializable
data class LocationDetails(
        var latitude: Double?,
        var longitude: Double?)


@Serializable
data class UserSocialProfile(
        var sid: Long?,
        var profileName: String? = "",
        var profileImgUrl: String?,
        var tokenId: String?,
        var teamIdServer: Long?,
        var affiliationId: Long?
)

@Serializable
data class UserOrderProfile(
        var sid: Long,
        var profileName: String?,
        var email: String?,
        var mobile: String?,
)

@Serializable
data class SessionSummary (
        val challengeName: String,
        val steps: Int,
        val calories: Int,
        val distance: Double,
        val heartRate: Int,
        val durationInMins: String,
        val duration: Int,
        var totalHr: String
        )

@Serializable
data class OrderDetails (
        val orderId: String,
        val key: String,
        val name: String,
        val desc: String,
        val amount: String,
        val currency: String,
        var userName: String?,
        var email: String?,
        var contact: String?,
        var address: String?,
        var pincode: String?,
        val sid: Long,
        val challengeId: Long,
        val challengeName: String,
        var packageId: Int?,
        )