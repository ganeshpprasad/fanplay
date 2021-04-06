package com.fanplayiot.core.db.local.entity

import androidx.annotation.IntDef
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

@Entity
class User {
    @PrimaryKey
    var id: Int? = null
    var tokenId: String? = null
    var sid: Long? = null
    var latitude = 0.0
    var longitude = 0.0
    var timeZone: String? = null
    var age = 0
        get() = if (field == 0) 30 else field
    var lastUpdated: Long = 0

    @get:LoginType
    var loginType = 0
    var profileName // User display name
            : String? = null
    var profileImgUrl: String? = null
    var teamPref: Long = 0
    var gender: String? = null
    var mobile: String? = null
    var email: String? = null
    var dob: String? = null
    var city: String? = null
    var height: String? = null
    var heightMeasure: String? = null
    var weight: String? = null
    var weightMeasure: String? = null
    var deviceId: String? = null
    var phoneDeviceInfo: String? = null

    //return "" + getId() + "," + getTokenId() + "," + getTimeZone() + "," + getLastUpdated();
    val rowAsString: String
        get() =//return "" + getId() + "," + getTokenId() + "," + getTimeZone() + "," + getLastUpdated();
            ("" + id + "," + tokenId + "," + timeZone
                    + "," + latitude + "," + longitude + "," +
                    profileName + "," + lastUpdated)

    companion object {
        const val EMAIL_LOGIN = 1
        const val OTP_LOGIN = 2
        const val ID_PROVIDER_LOGIN = 3
    }
}

@Retention(RetentionPolicy.SOURCE)
@IntDef(User.EMAIL_LOGIN, User.OTP_LOGIN, User.ID_PROVIDER_LOGIN)
annotation class LoginType