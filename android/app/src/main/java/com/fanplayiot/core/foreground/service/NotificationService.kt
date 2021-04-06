package com.fanplayiot.core.foreground.service

import android.app.Notification
import android.app.Person
import android.bluetooth.BluetoothProfile
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.demoproject.R
import com.fanplayiot.core.bluetooth.SDKManager
import com.fanplayiot.core.db.local.repository.FeatureStorage
//import com.fanplayiot.core.ui.bandfeature.NotifyFragment
import com.fanplayiot.core.utils.NotificationUtils
import com.mcube.ms.sdk.definitions.MSDefinition

class NotificationService : NotificationListenerService() {

    private lateinit var sdk: SDKManager
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName ?: return
        var extraText = sbn.notification?.extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: run {
            Log.i(TAG, "notification text is null")
            return
        }

        val extraTitle = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            val extras = sbn.notification?.extras
            // sbn.notification?.extras?.get(Notification.EXTRA_MESSAGING_PERSON)?.toString()
            val person = extras?.getParcelable<Person>(Notification.EXTRA_MESSAGING_PERSON)
            person?.name?.toString()
        } else {
            sbn.notification?.extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        }
        extraTitle?.let {if (extraText.contains(it)) { extraText = "$extraTitle: $extraText" } }
        Log.d(TAG, "sbn id  ${sbn.id} app $packageName message")
        val matchId = packageNames.filter {(_, value) -> value.contains(packageName) }.keys.firstOrNull()
        matchId?.let { keyValue ->
            if (!FeatureStorage(applicationContext).isEnabledNotify(keyValue)) return

            /*if (!SDKManager.instance().userModule.setOtherAppNotify(keyValue, getString(strResId[keyValue]!!))) {
                SDKManager.instance().firmware.readFirmwareVersion()
                return
            }*/
            if (keyValue == MS_DEF_INCOMING_SMS) {
                if (!sdk.userModule.setSMSNotify(extraText)) {
                    //if failed reset param
                    sdk.firmware.readFirmwareVersion()
                }
                return
            }
            if (!sdk.userModule.setOtherAppNotify(keyValue, getString(strResId[keyValue]!!))) {
                //if failed reset param
                sdk.firmware.readFirmwareVersion()
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //stopForeground(true)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand()")
        sdk = SDKManager.instance(applicationContext)
        /*if (SDKManager.instance().ble.connectionState == BluetoothProfile.STATE_CONNECTED) {
            startForeground(NotificationUtils.NOTIFICATION_BAND_FEATURE_ID,
                    NotificationUtils.startForegroundNotify(applicationContext,
                            getString(R.string.notify_band_connected),
                            NotificationUtils.NOTIFICATION_BAND_FEATURE_ID
                    ))
        }*/
        return START_STICKY
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {

    }

    override fun onListenerConnected() {
        Log.d(TAG, "onListenerConnected()")
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)
    }

    companion object {
        /* Debugging */
        private val TAG = NotificationService::class.java.simpleName

        /* Key */
        private const val PACKAGENAME_WECHAT = "com.tencent.mm"
        private const val PACKAGENAME_QQ = "com.tencent.mobileqq"
        private const val PACKAGENAME_QQ_I = "com.tencent.mobileqqi"
        private const val PACKAGENAME_QQ_HD = "com.tencent.minihd.qq"
        private const val PACKAGENAME_QQ_LITE = "com.tencent.qqlite"
        private const val PACKAGENAME_FACEBOOK = "com.facebook.katana"
        private const val PACKAGENAME_FACEBOOK_LITE = "com.facebook.lite"
        private const val PACKAGENAME_MESSAGE = "com.facebook.orca" // FB msg app.
        private const val PACKAGENAME_MESSAGE_LITE = "com.facebook.mlite" // FB msg app.
        private const val PACKAGENAME_TWITTER = "com.twitter.android"
        private const val PACKAGENAME_WHATSAPP = "com.whatsapp"
        private const val PACKAGENAME_WHATSAPP_BUSINESS = "com.whatsapp.w4b"
        private const val PACKAGENAME_INSTAGRAM = "com.instagram.android"
        private const val PACKAGENAME_LINE = "jp.naver.line.android"
        private const val PACKAGENAME_SMS = "com.google.android.apps.messaging"
        private const val PACKAGENAME_SMS_1 = "com.zndroid"
        private const val PACKAGENAME_SMS_2 = "com.android.mms"
        private val packageNames = mapOf(
                MSDefinition.OTHER_APP_WECHAT to arrayOf(PACKAGENAME_WECHAT),
                MSDefinition.OTHER_APP_QQ to arrayOf(PACKAGENAME_QQ, PACKAGENAME_QQ_I, PACKAGENAME_QQ_HD, PACKAGENAME_QQ_LITE),
                MSDefinition.OTHER_APP_FACEBOOK to arrayOf(PACKAGENAME_FACEBOOK, PACKAGENAME_FACEBOOK_LITE, PACKAGENAME_MESSAGE, PACKAGENAME_MESSAGE_LITE),
                MSDefinition.OTHER_APP_TWITTER to arrayOf(PACKAGENAME_TWITTER),
                MSDefinition.OTHER_APP_WHATSAPP to arrayOf(PACKAGENAME_WHATSAPP, PACKAGENAME_WHATSAPP_BUSINESS),
                MSDefinition.OTHER_APP_INSTAGRAM to arrayOf(PACKAGENAME_INSTAGRAM),
                MSDefinition.OTHER_APP_LINE to arrayOf(PACKAGENAME_LINE),
                17 to arrayOf(PACKAGENAME_SMS, PACKAGENAME_SMS_1, PACKAGENAME_SMS_2)
        )

        private val strResId = mapOf(
                MSDefinition.OTHER_APP_WECHAT to R.string.wechat,
                MSDefinition.OTHER_APP_QQ to R.string.qq,
                MSDefinition.OTHER_APP_FACEBOOK to R.string.facebook,
                MSDefinition.OTHER_APP_TWITTER to R.string.twitter,
                MSDefinition.OTHER_APP_WHATSAPP to R.string.whatsapp,
                MSDefinition.OTHER_APP_INSTAGRAM to R.string.instagram,
                MSDefinition.OTHER_APP_LINE to R.string.line,
                17 to R.string.sms
        )
        private const val MS_DEF_INCOMING_CALL = 16
        private const val MS_DEF_INCOMING_SMS = 17

    }
}
