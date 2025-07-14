package dev.pegasus.whatsappfilerecovery.manager

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.FileObserver
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
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

    private val treeUri by lazy { (context.getSharedPreferences("permission_preferences", Context.MODE_PRIVATE)).getString("document_tree_uri", "")?.toUri() }

    private val observers = mutableListOf<FileObserver>()

    private val backupDir = File(context.filesDir, "file_backup")
    private val recoveryDir = File(context.filesDir, "file_recovery")

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
        val root = Environment.getExternalStorageDirectory()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            listOf(
                "$root/Android/media/com.whatsapp/WhatsApp/Media/Whatsapp Images/",
                "$root/Android/media/com.whatsapp.w4b/WhatsApp Business/Media/WhatsApp Business Images/",
                "$root/Android/media/com.whatsapp/WhatsApp/Media/Whatsapp Video/",
                "$root/Android/media/com.whatsapp.w4b/WhatsApp Business/Media/WhatsApp Business Video",
                "$root/Android/media/com.whatsapp/WhatsApp/Media/Whatsapp Audio/",
                "$root/Android/media/com.whatsapp.w4b/WhatsApp Business/Media/WhatsApp Business Audio",
                "$root/Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Voice Notes/",
                "$root/Android/media/com.whatsapp.w4b/WhatsApp Business/Media/WhatsApp Business Voice Notes",
                "$root/Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Documents/",
                "$root/Android/media/com.whatsapp.w4b/WhatsApp Business/Media/WhatsApp Business Documents",
                "$root/Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Stickers/",
                "$root/Android/media/com.whatsapp.w4b/WhatsApp Business/Media/WhatsApp Business Stickers",
                "$root/Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Animated Gifs/",
                "$root/Android/media/com.whatsapp.w4b/WhatsApp Business/Media/WhatsApp Business Animated Gifs",
            )
        else
            listOf(
                "$root/Android/media/com.whatsapp/WhatsApp/Media/Whatsapp Images/",
                "$root/Android/media/com.whatsapp.w4b/WhatsApp Business/Media/WhatsApp Business Images/",
                "$root/Android/media/com.whatsapp/WhatsApp/Media/Whatsapp Video/",
                "$root/Android/media/com.whatsapp.w4b/WhatsApp Business/Media/WhatsApp Business Video",
                "$root/Android/media/com.whatsapp/WhatsApp/Media/Whatsapp Audio/",
                "$root/Android/media/com.whatsapp.w4b/WhatsApp Business/Media/WhatsApp Business Audio",
                "$root/Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Voice Notes/",
                "$root/Android/media/com.whatsapp.w4b/WhatsApp Business/Media/WhatsApp Business Voice Notes",
                "$root/Android/media/com.whatsapp/WhatsApp/Media/Whatsapp Documents/",
                "$root/Android/media/com.whatsapp.w4b/WhatsApp Business/Media/WhatsApp Business Documents",
                "$root/Android/media/com.whatsapp/WhatsApp/Media/Whatsapp Stickers/",
                "$root/Android/media/com.whatsapp.w4b/WhatsApp Business/Media/WhatsApp Business Stickers",
                "$root/Android/media/com.whatsapp/WhatsApp/Media/Whatsapp Animated Gifs/",
                "$root/Android/media/com.whatsapp.w4b/WhatsApp Business/Media/WhatsApp Business Animated Gifs",
                "$root/WhatsApp/Media/WhatsApp Images",
                "$root/WhatsApp Business/Media/WhatsApp Business Images",
                "$root/WhatsApp/Media/WhatsApp Video",
                "$root/WhatsApp Business/Media/WhatsApp Business Video",
                "$root/WhatsApp/Media/WhatsApp Audio",
                "$root/WhatsApp Business/Media/WhatsApp Business Audio",
                "$root/WhatsApp/Media/WhatsApp Voice Notes",
                "$root/WhatsApp Business/Media/WhatsApp Business Voice Notes",
                "$root/WhatsApp/Media/WhatsApp Documents",
                "$root/WhatsApp Business/Media/WhatsApp Business Documents",
                "$root/WhatsApp/Media/WhatsApp Stickers",
                "$root/WhatsApp Business/Media/WhatsApp Business Stickers",
                "$root/WhatsApp/Media/WhatsApp Animated Gifs",
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
    inner class MediaObserverHigher(private val folder: File) : FileObserver(folder, CREATE or MODIFY or DELETE or MOVED_TO or ATTRIB or ACCESS) {

        override fun onEvent(event: Int, path: String?) {
            if (path.isNullOrEmpty()) return
            val eventFile = File(folder, path)

            when (event) {
                CREATE, MODIFY -> {
                    applicationScope.launch { eventFile.copySafely(context, backupDir, treeUri!!) }
                }

                DELETE -> {
                    applicationScope.launch {
                        val cached = File(backupDir, eventFile.name)
                        if (cached.exists()) {
                            cached.copySafely(context, recoveryDir, treeUri!!)
                            notificationManager.postNotificationRecovery(cached)
                        } else {
                            Log.e(TAG, "MediaObserverHigher: onEvent: file: $cached not exist.")
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