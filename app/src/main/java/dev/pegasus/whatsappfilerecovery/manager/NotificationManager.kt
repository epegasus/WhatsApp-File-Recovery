package dev.pegasus.whatsappfilerecovery.manager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import dev.pegasus.whatsappfilerecovery.MainActivity
import dev.pegasus.whatsappfilerecovery.R
import dev.pegasus.whatsappfilerecovery.utils.ConstantUtils.TAG
import java.io.File

/**
 * Created by: Sohaib Ahmed
 * Date: 6/30/2025
 * <p>
 * Links:
 * - LinkedIn: <a href="https://linkedin.com/in/epegasus">Linkedin</a>
 * - GitHub: <a href="https://github.com/epegasus">Github</a>
 */

class NotificationManager(private val context: Context) {

    private val notificationManager by lazy { context.getSystemService(NotificationManager::class.java) }
    private val channelIdService = "media_detector_channel"
    private val channelIdRecovery = "media_recover_channel"

    init {
        createChannels()
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelService = NotificationChannel(channelIdService, "Media Detector", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Working on background for the detection of deleted media (to recover)."
                lockscreenVisibility = NotificationCompat.VISIBILITY_PRIVATE
            }
            val channelRecovery = NotificationChannel(channelIdService, "Media Recovery", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Notifications will be post on any delete media recovery."
                lockscreenVisibility = NotificationCompat.VISIBILITY_PRIVATE
            }

            notificationManager?.createNotificationChannel(channelService)
            notificationManager?.createNotificationChannel(channelRecovery)
        }
    }

    fun createNotificationService(): Notification {
        val notificationIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val flags = PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, flags)

        return NotificationCompat.Builder(context, channelIdService)
            .setContentTitle("Detecting Deleted Media")
            .setContentText("This app is actively monitoring and recovering deleted media.")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOngoing(true)
            .build()
    }

    fun postNotificationRecovery(file: File) {
        Log.d(TAG, "postNotificationRecovery: $file")
        val notificationIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val flags = PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, flags)

        val notification = NotificationCompat.Builder(context, channelIdRecovery)
            .setContentTitle("File Recovered")
            .setContentText("Deleted media has been recovered successfully.")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setAutoCancel(true)
            .setOngoing(true)
            .build()
        notificationManager.notify(101, notification)
    }
}