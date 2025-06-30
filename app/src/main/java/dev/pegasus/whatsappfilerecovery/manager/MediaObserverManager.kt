package dev.pegasus.whatsappfilerecovery.manager

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.FileObserver
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LifecycleCoroutineScope
import dev.pegasus.whatsappfilerecovery.utils.ConstantUtils.TAG
import dev.pegasus.whatsappfilerecovery.utils.copySafely
import kotlinx.coroutines.launch
import java.io.File

/**
 * Created by: Sohaib Ahmed
 * Date: 6/30/2025
 * <p>
 * Links:
 * - LinkedIn: <a href="https://linkedin.com/in/epegasus">Linkedin</a>
 * - GitHub: <a href="https://github.com/epegasus">Github</a>
 */

class MediaObserverManager(private val context: Context, private val notificationManager: NotificationManager, private val applicationScope: LifecycleCoroutineScope) {

    private val observers = mutableListOf<FileObserver>()

    private val backupDir = File(context.filesDir, "File Backup")
    private val recoveryDir = File(context.filesDir, "File Recovery")

    fun startObserving() {
        Log.d(TAG, "MediaObserver: startObserving: Service Started")
        getFoldersToObserve().forEach { folderPath ->
            createObserver(folderPath)?.let {
                observers.add(it)
                it.startWatching()
            }
        }
    }

    fun stopObserving() {
        Log.e(TAG, "MediaObserver: stopObserving: Service Stopped")
        observers.forEach { it.stopWatching() }
        observers.clear()
    }

    private fun getFoldersToObserve(): List<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            listOf(
                "${Environment.getExternalStorageDirectory()}/Android/media/com.whatsapp/WhatsApp/Media/Whatsapp Images/",
                "${Environment.getExternalStorageDirectory()}/Android/media/com.whatsapp.w4b/WhatsApp Business/Media/WhatsApp Business Images/",
                "${Environment.getExternalStorageDirectory()}/Android/media/com.whatsapp/WhatsApp/Media/Whatsapp Video/",
                "${Environment.getExternalStorageDirectory()}/Android/media/com.whatsapp.w4b/WhatsApp Business/Media/WhatsApp Business Video",
                "${Environment.getExternalStorageDirectory()}/Android/media/com.whatsapp/WhatsApp/Media/Whatsapp Audio/",
                "${Environment.getExternalStorageDirectory()}/Android/media/com.whatsapp.w4b/WhatsApp Business/Media/WhatsApp Business Audio",
                "${Environment.getExternalStorageDirectory()}/Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Voice Notes/",
                "${Environment.getExternalStorageDirectory()}/Android/media/com.whatsapp.w4b/WhatsApp Business/Media/WhatsApp Business Voice Notes",
                "${Environment.getExternalStorageDirectory()}/Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Documents/",
                "${Environment.getExternalStorageDirectory()}/Android/media/com.whatsapp.w4b/WhatsApp Business/Media/WhatsApp Business Documents",
                "${Environment.getExternalStorageDirectory()}/Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Stickers/",
                "${Environment.getExternalStorageDirectory()}/Android/media/com.whatsapp.w4b/WhatsApp Business/Media/WhatsApp Business Stickers",
                "${Environment.getExternalStorageDirectory()}/Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Animated Gifs/",
                "${Environment.getExternalStorageDirectory()}/Android/media/com.whatsapp.w4b/WhatsApp Business/Media/WhatsApp Business Animated Gifs",
            )
        else
            listOf(
                "${Environment.getExternalStorageDirectory()}/Android/media/com.whatsapp/WhatsApp/Media/Whatsapp Images/",
                "${Environment.getExternalStorageDirectory()}/Android/media/com.whatsapp.w4b/WhatsApp Business/Media/WhatsApp Business Images/",
                "${Environment.getExternalStorageDirectory()}/Android/media/com.whatsapp/WhatsApp/Media/Whatsapp Video/",
                "${Environment.getExternalStorageDirectory()}/Android/media/com.whatsapp.w4b/WhatsApp Business/Media/WhatsApp Business Video",
                "${Environment.getExternalStorageDirectory()}/Android/media/com.whatsapp/WhatsApp/Media/Whatsapp Audio/",
                "${Environment.getExternalStorageDirectory()}/Android/media/com.whatsapp.w4b/WhatsApp Business/Media/WhatsApp Business Audio",
                "${Environment.getExternalStorageDirectory()}/Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Voice Notes/",
                "${Environment.getExternalStorageDirectory()}/Android/media/com.whatsapp.w4b/WhatsApp Business/Media/WhatsApp Business Voice Notes",
                "${Environment.getExternalStorageDirectory()}/Android/media/com.whatsapp/WhatsApp/Media/Whatsapp Documents/",
                "${Environment.getExternalStorageDirectory()}/Android/media/com.whatsapp.w4b/WhatsApp Business/Media/WhatsApp Business Documents",
                "${Environment.getExternalStorageDirectory()}/Android/media/com.whatsapp/WhatsApp/Media/Whatsapp Stickers/",
                "${Environment.getExternalStorageDirectory()}/Android/media/com.whatsapp.w4b/WhatsApp Business/Media/WhatsApp Business Stickers",
                "${Environment.getExternalStorageDirectory()}/Android/media/com.whatsapp/WhatsApp/Media/Whatsapp Animated Gifs/",
                "${Environment.getExternalStorageDirectory()}/Android/media/com.whatsapp.w4b/WhatsApp Business/Media/WhatsApp Business Animated Gifs",
                "${Environment.getExternalStorageDirectory()}/WhatsApp/Media/WhatsApp Images",
                "${Environment.getExternalStorageDirectory()}/WhatsApp Business/Media/WhatsApp Business Images",
                "${Environment.getExternalStorageDirectory()}/WhatsApp/Media/WhatsApp Video",
                "${Environment.getExternalStorageDirectory()}/WhatsApp Business/Media/WhatsApp Business Video",
                "${Environment.getExternalStorageDirectory()}/WhatsApp/Media/WhatsApp Audio",
                "${Environment.getExternalStorageDirectory()}/WhatsApp Business/Media/WhatsApp Business Audio",
                "${Environment.getExternalStorageDirectory()}/WhatsApp/Media/WhatsApp Voice Notes",
                "${Environment.getExternalStorageDirectory()}/WhatsApp Business/Media/WhatsApp Business Voice Notes",
                "${Environment.getExternalStorageDirectory()}/WhatsApp/Media/WhatsApp Documents",
                "${Environment.getExternalStorageDirectory()}/WhatsApp Business/Media/WhatsApp Business Documents",
                "${Environment.getExternalStorageDirectory()}/WhatsApp/Media/WhatsApp Stickers",
                "${Environment.getExternalStorageDirectory()}/WhatsApp Business/Media/WhatsApp Business Stickers",
                "${Environment.getExternalStorageDirectory()}/WhatsApp/Media/WhatsApp Animated Gifs",
                "${Environment.getExternalStorageDirectory()}/WhatsApp Business/Media/WhatsApp Business Animated Gifs",
            )
    }

    /**
     *  3 versions
     *      1) Android 11 or higher
     *      2) Android 10
     *      3) Android 9 or lower
     */
    private fun createObserver(folderPath: String): FileObserver? {
        val folder = File(folderPath)

        if (!folder.exists() || !folder.isDirectory) return null

        val isVoiceNote = folderPath.contains("Voice Notes")
        val isScopedStorage = folderPath.contains("/Android/media/")

        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                if (isVoiceNote) MediaObserverHigherVoiceNote(folder)
                else MediaObserverHigher(folder)
            }

            // if Scope Storage and voice notes, need to traverse through sub-directories
            Build.VERSION.SDK_INT == Build.VERSION_CODES.Q -> {
                if (isScopedStorage && isVoiceNote) MediaObserverHigherVoiceNote(folder)
                else MediaObserver(folderPath)
            }

            else -> MediaObserver(folderPath)
        }
    }

    @Suppress("DEPRECATION")
    inner class MediaObserver(private val folderPath: String) : FileObserver(folderPath, CREATE or MODIFY or MOVED_TO or DELETE) {

        override fun onEvent(event: Int, path: String?) {
            if (path.isNullOrEmpty()) return
            val eventFile = File("$folderPath/$path")

            when (event) {
                CREATE, MODIFY, MOVED_TO -> {
                    applicationScope.launch { eventFile.copySafely(context, backupDir) }
                }

                DELETE -> {
                    applicationScope.launch {
                        val cached = File(backupDir, eventFile.name)
                        if (cached.exists()) {
                            cached.copySafely(context, recoveryDir)
                            notificationManager.postNotificationRecovery(cached)
                        }
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    inner class MediaObserverHigher(private val folder: File) : FileObserver(folder, CREATE or MODIFY or DELETE or MOVED_TO or ATTRIB or ACCESS) {

        override fun onEvent(event: Int, path: String?) {
            if (path.isNullOrEmpty()) return
            val eventFile = File(folder, path)

            when (event) {
                CREATE, MODIFY -> {
                    applicationScope.launch { eventFile.copySafely(context, backupDir) }
                }

                DELETE -> {
                    applicationScope.launch {
                        val cached = File(backupDir, eventFile.name)
                        if (cached.exists()) {
                            cached.copySafely(context, recoveryDir)
                            notificationManager.postNotificationRecovery(cached)
                        }
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    inner class MediaObserverHigherVoiceNote(private val folder: File) : FileObserver(folder, CREATE or MODIFY or DELETE or MOVED_TO or ATTRIB or ACCESS) {

        private var latestObserver: FileObserver? = null
        private var latestObservedFolder: File? = null

        override fun onEvent(event: Int, path: String?) {
            if (path.isNullOrEmpty()) return
            val eventFile = File(folder, path)

            when (event) {
                CREATE, MODIFY, 1073741840, 1073741856 -> {
                    if (eventFile.isDirectory) {
                        observeLatestModifiedFolder()
                    }
                }
            }
        }

        private fun observeLatestModifiedFolder() {
            val latestFolder = folder.listFiles()
                ?.filter { it.isDirectory }
                ?.maxByOrNull { it.lastModified() } // Get the most recently modified folder

            if (latestFolder != null && latestFolder != latestObservedFolder) {
                latestObservedFolder = latestFolder
                latestObserver?.stopWatching() // Stop observing previous folder if exists
                latestObserver = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    MediaObserverHigher(latestFolder)
                } else {
                    MediaObserver(latestFolder.path)
                }
                latestObserver?.startWatching()
            }
        }
    }
}