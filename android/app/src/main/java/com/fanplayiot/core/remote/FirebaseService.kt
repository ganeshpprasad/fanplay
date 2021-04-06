package com.fanplayiot.core.remote

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.demoproject.R
import com.fanplayiot.core.db.local.repository.UserProfileStorage
import com.fanplayiot.core.utils.NotificationUtils
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseService : FirebaseMessagingService() {
    private lateinit var broadcastManager: LocalBroadcastManager

    override fun onCreate() {
        super.onCreate()
        broadcastManager = LocalBroadcastManager.getInstance(this)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val nhMessage: String?
        var notifTitle: String? = null
        var notifImage = ""
        var notifyAction = ""
        var notifTopic = remoteMessage.from
        Log.d(TAG, "Message Notification From: " + remoteMessage.from)
        Log.d(TAG, "Message Notification Data: " + remoteMessage.data.toString())
        //Log.d(TAG, "Message Notification Type: " + remoteMessage.messageType)
        val data = remoteMessage.data

        // Check if message contains a notification payload.
        if (remoteMessage.notification != null) {
            nhMessage = remoteMessage.notification!!.body
        } else {
            nhMessage = data["body"]
            notifTitle = data["title"]
            notifImage = data["data"] ?: ""
            //nhMessage = remoteMessage.getData().values().iterator().next();
        }

        val optionalData = remoteMessage.data
        optionalData[CUSTOM_ACTION]?.let { value ->
            if (value == ACTION_HR) {
                notifyAction = value
                Log.d(TAG, "action received: $value")
            }
        }

        Log.d(TAG, "Message Notification Body: $nhMessage")
        if (notifTopic == TOPIC) {
            notifImage = remoteMessage.notification!!.imageUrl?.toString() ?: ""
            notifTitle = remoteMessage.notification!!.title
            val intent = Intent(FAN_OF_HOUR)
            val bundle = Bundle().apply {
                putString("body", nhMessage)
                putString("title", notifTitle)
                putString("image", notifImage)
                putString(CUSTOM_ACTION, notifyAction)
            }
            intent.putExtra(EXTRA_BUNDLE_FCM, bundle)
            broadcastManager.sendBroadcast(intent)
        } else if (notifTopic === "azure") {
            val intent = Intent(FAN_OF_HOUR)
            intent.putExtra("body", nhMessage)
            intent.putExtra("title", notifTitle)
            intent.putExtra("image", notifImage)
            broadcastManager.sendBroadcast(intent)
        } else {
            if (nhMessage != null && nhMessage.isNotEmpty()) {
                Log.d(TAG, "FCM message $nhMessage")
                val bundle = Bundle().apply {
                    putString("action", notifyAction)
                }
                NotificationUtils.notifyOnReceived(this, nhMessage, bundle)
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, token)
        val storage = UserProfileStorage(applicationContext)
        storage.fcmToken = token
    }

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "FP_NOTIFICATION_CHANNEL"
        private const val TAG = "FirebaseService"
        const val FAN_OF_HOUR = "fan_of_hour"
        const val ACTION_HR = "fe_start_hr"
        const val CUSTOM_ACTION = "action"
        const val EXTRA_BUNDLE_FCM = "EXTRA_BUNDLE_FCM"
        const val TOPIC = "/topics/fan_of_hour"
        //private val tags = listOf("fan_of_the_hour", "rewards", "normal_notifications", "event_based", "stats")

        @JvmStatic
        fun createChannelAndHandleNotifications(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                        NOTIFICATION_CHANNEL_ID,
                        context.getString(R.string.channel_name),
                        NotificationManager.IMPORTANCE_HIGH)
                channel.description = context.getString(R.string.channel_desc)
                channel.setShowBadge(true)
                val notificationManager = context.getSystemService(NotificationManager::class.java)
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
}