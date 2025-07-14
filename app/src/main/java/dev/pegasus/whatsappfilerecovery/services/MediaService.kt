package dev.pegasus.whatsappfilerecovery.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import dev.pegasus.whatsappfilerecovery.manager.MediaObserverManager
import dev.pegasus.whatsappfilerecovery.manager.NotificationManager

class MediaService : Service() {

    private val applicationScope = ProcessLifecycleOwner.get().lifecycleScope
    private val notificationManager by lazy { NotificationManager(this) }
    private val mediaObserverManager by lazy { MediaObserverManager(this, notificationManager, applicationScope) }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        postNotification()
        initMediaObserver()
        return START_STICKY
    }

    private fun postNotification() {
        val notification = NotificationManager(this).createNotificationService()
        startForeground(100, notification)
    }

    private fun initMediaObserver() {
        mediaObserverManager.startObserving()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaObserverManager.stopObserving()
    }
}