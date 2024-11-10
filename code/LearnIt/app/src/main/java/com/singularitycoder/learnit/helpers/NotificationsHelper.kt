package com.singularitycoder.learnit.helpers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import androidx.core.app.NotificationCompat
import com.singularitycoder.learnit.R
import java.util.Date

internal object NotificationsHelper {

    const val NOTIFICATION_CHANNEL_ID = "learn_it_notification_channel"
    const val IMPORT_EXPORT_NOTIFICATION_ID: Int = 101010
    const val ALARM_NOTIFICATION_ID: Int = 101011

    // https://stackoverflow.com/questions/12978184/android-get-unique-id-of-notification#comment51322954_28251192
    fun getUniqueNotifID(): Int {
        return ((Date().time / 1000L) % Int.MAX_VALUE).toInt()
    }

    fun createNotificationChannel(context: Context) {
        val notificationManager = context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager

        // create the notification channel
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        )
//        channel.setSound(null, null)
        notificationManager.createNotificationChannel(channel)
    }

    fun createNotification(
        context: Context,
        title: String?
    ): Notification {
        val notification: Notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID).apply {
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setSilent(true)
//            setVibrate()
//            setSound()
            setContentTitle(title)
            setPriority(NotificationCompat.PRIORITY_HIGH)
            setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            setSmallIcon(R.drawable.ic_app_icon)
//            setOngoing(true)
        }.build()

        return notification
    }

    /** Update view state in notification */
    fun updateNotification(
        context: Context,
        title: String?,
        notifId: Int
    ) {
        val notification = createNotification(context, title)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        notificationManager?.notify(notifId, notification)
    }

    fun clearNotification(
        context: Context,
        notifId: Int
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notifId)
    }
}
