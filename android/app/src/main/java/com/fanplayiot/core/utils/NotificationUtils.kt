package com.fanplayiot.core.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.demoproject.MainActivity
import com.demoproject.R

object NotificationUtils {
    private const val CHANNEL_ID = "FP_NOTIFICATION_CHANNEL"

    //private const val EXTRA_NOTIFICATION_ID = "EXTRA_NOTIFICATION_ID"
    private const val NOTIFICATION_ID = 1
    const val DEVICE_SERVICE_ACTION = "com.fanplayiot.core.DEVICE_SERVICE_ACTION"

    //const val NOTIFICATION_BAND_FEATURE_ID = 2
    const val NOTIFICATION_SERVICE_READ_HR_ID = 3

    fun notifyOnReceived(context: Context, message: String, bundle: Bundle) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtras(bundle)
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
        createNotificationChannel(context)

        //Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_fanplay_logo_h_01);
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_fanplay_logo_symbol)
                .setContentTitle(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
        val managerCompat = NotificationManagerCompat.from(context)
        managerCompat.notify(NOTIFICATION_ID, builder.build())
    }

    fun startForegroundNotify(context: Context, message: String, notificationId: Int,
                              bundle: Bundle = Bundle(), contentText: String? = null): Notification {
        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        val stopHrIntent = Intent(context, MainActivity::class.java).apply {
            action = DEVICE_SERVICE_ACTION
            putExtras(bundle)
        }
        //val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
        val stopHrPendingIntent = PendingIntent.getActivity(context, 0, stopHrIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        createNotificationChannel(context)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_fanplay_logo_symbol)
                .setContentTitle(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(stopHrPendingIntent)
                .setAutoCancel(false)
                .setOnlyAlertOnce(true)

        if (NOTIFICATION_SERVICE_READ_HR_ID == notificationId) {
            builder
                    .setContentIntent(null)
                    .addAction(R.drawable.ic_stop, context.getText(R.string.stop), stopHrPendingIntent)
            if (contentText != null) {
                builder.setContentText(contentText)
                builder.setStyle(
                        NotificationCompat.BigTextStyle().bigText(contentText))
            }
        }
        return builder.build()
    }

    private fun createNotificationChannel(context: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = context.getString(R.string.channel_name)
            val description = context.getString(R.string.channel_desc)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}