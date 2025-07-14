package dev.pegasus.whatsappfilerecovery.manager

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.FileObserver
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LifecycleCoroutineScope
import dev.pegasus.whatsappfilerecovery.utils.ConfigUtils.backupDir
import dev.pegasus.whatsappfilerecovery.utils.ConfigUtils.recoveryDir
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

    private val fileManager by lazy { FileManager(context, notificationManager) }
    private val observers = mutableListOf<FileObserver>()

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
        val root = Environment.getExternalStorageDirectory().absolutePath
        val mediaRoot = Environment.getExternalStorageDirectory().absolutePath + "/Android/media/"
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            listOf(
                "$mediaRoot/com.whatsapp/WhatsApp/Media/Whatsapp Images/",
                "$mediaRoot/com.whatsapp/WhatsApp/Media/Whatsapp Video/",
                "$mediaRoot/com.whatsapp/WhatsApp/Media/Whatsapp Audio/",
                "$mediaRoot/com.whatsapp/WhatsApp/Media/WhatsApp Voice Notes/",
                "$mediaRoot/com.whatsapp/WhatsApp/Media/WhatsApp Documents/",
                "$mediaRoot/com.whatsapp/WhatsApp/Media/WhatsApp Stickers/",
                "$mediaRoot/com.whatsapp/WhatsApp/Media/WhatsApp Animated Gifs/",
                "$mediaRoot/com.whatsapp.w4b/WhatsApp Business/Media/WhatsApp Business Images/",
                "$mediaRoot/com.whatsapp.w4b/WhatsApp Business/Media/WhatsApp Business Video",
                "$mediaRoot/com.whatsapp.w4b/WhatsApp Business/Media/WhatsApp Business Audio",
                "$mediaRoot/com.whatsapp.w4b/WhatsApp Business/Media/WhatsApp Business Voice Notes",
                "$mediaRoot/com.whatsapp.w4b/WhatsApp Business/Media/WhatsApp Business Documents",
                "$mediaRoot/com.whatsapp.w4b/WhatsApp Business/Media/WhatsApp Business Stickers",
                "$mediaRoot/com.whatsapp.w4b/WhatsApp Business/Media/WhatsApp Business Animated Gifs",
            )
        else
            listOf(
                "$mediaRoot/com.whatsapp/WhatsApp/Media/Whatsapp Images/",
                "$mediaRoot/com.whatsapp/WhatsApp/Media/Whatsapp Video/",
                "$mediaRoot/com.whatsapp/WhatsApp/Media/Whatsapp Audio/",
                "$mediaRoot/com.whatsapp/WhatsApp/Media/WhatsApp Voice Notes/",
                "$mediaRoot/com.whatsapp/WhatsApp/Media/WhatsApp Documents/",
                "$mediaRoot/com.whatsapp/WhatsApp/Media/WhatsApp Stickers/",
                "$mediaRoot/com.whatsapp/WhatsApp/Media/WhatsApp Animated Gifs/",
                "$mediaRoot/com.whatsapp.w4b/WhatsApp Business/Media/WhatsApp Business Images/",
                "$mediaRoot/com.whatsapp.w4b/WhatsApp Business/Media/WhatsApp Business Video",
                "$mediaRoot/com.whatsapp.w4b/WhatsApp Business/Media/WhatsApp Business Audio",
                "$mediaRoot/com.whatsapp.w4b/WhatsApp Business/Media/WhatsApp Business Voice Notes",
                "$mediaRoot/com.whatsapp.w4b/WhatsApp Business/Media/WhatsApp Business Documents",
                "$mediaRoot/com.whatsapp.w4b/WhatsApp Business/Media/WhatsApp Business Stickers",
                "$mediaRoot/com.whatsapp.w4b/WhatsApp Business/Media/WhatsApp Business Animated Gifs",
                "$root/WhatsApp/Media/WhatsApp Images",
                "$root/WhatsApp/Media/WhatsApp Video",
                "$root/WhatsApp/Media/WhatsApp Audio",
                "$root/WhatsApp/Media/WhatsApp Voice Notes",
                "$root/WhatsApp/Media/WhatsApp Documents",
                "$root/WhatsApp/Media/WhatsApp Stickers",
                "$root/WhatsApp/Media/WhatsApp Animated Gifs",
                "$root/WhatsApp Business/Media/WhatsApp Business Images",
                "$root/WhatsApp Business/Media/WhatsApp Business Video",
                "$root/WhatsApp Business/Media/WhatsApp Business Audio",
                "$root/WhatsApp Business/Media/WhatsApp Business Voice Notes",
                "$root/WhatsApp Business/Media/WhatsApp Business Documents",
                "$root/WhatsApp Business/Media/WhatsApp Business Stickers",
                "$root/WhatsApp Business/Media/WhatsApp Business Animated Gifs",
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

        val isVoiceNote = folderPath.contains("Voice Notes", ignoreCase = true)
        val isScopedStorage = folderPath.contains("/Android/media/", ignoreCase = true)

        Log.v(TAG, "MediaObserverManager: createObserver: Observing: $folderPath")
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
                    applicationScope.launch { eventFile.copySafely(context, backupDir, treeUri!!) }
                }

                DELETE -> {
                    applicationScope.launch {
                        val cached = File(backupDir, eventFile.name)
                        if (cached.exists()) {
                            cached.copySafely(context, recoveryDir, treeUri!!)
                            notificationManager.postNotificationRecovery(cached)
                        }
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    inner class MediaObserverHigherVoiceNote(private val folder: File) : FileObserver(folder, ALL_EVENTS) {

        private var latestObserver: FileObserver? = null
        private var latestObservedFolder: File? = null
        private var currentPath = ""

        override fun onEvent(event: Int, path: String?) {
            if (path.isNullOrEmpty()) return
            if (currentPath.isNotEmpty() && currentPath == path) return
            currentPath = path

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
                Log.d(TAG, "MediaObserverHigherVoiceNote: observeLatestModifiedFolder: Observing this one: $latestFolder")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    inner class MediaObserverHigher(private val folder: File) : FileObserver(folder.absolutePath, CREATE or MOVED_TO or DELETE) {
        override fun onEvent(event: Int, path: String?) {
            if (path.isNullOrEmpty()) return
            when (event) {
                CREATE, MOVED_TO -> applicationScope.launch { fileManager.copyFile(folder, path) }
                DELETE -> applicationScope.launch { fileManager.recoverFile(folder, path) }
            }
        }
    }
}