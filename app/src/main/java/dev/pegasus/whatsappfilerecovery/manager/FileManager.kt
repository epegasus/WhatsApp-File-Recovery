package dev.pegasus.whatsappfilerecovery.manager

import android.content.Context
import android.os.Build
import android.os.FileUtils
import android.util.Log
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import dev.pegasus.whatsappfilerecovery.utils.ConfigUtils.backupDir
import dev.pegasus.whatsappfilerecovery.utils.ConfigUtils.recoveryDir
import dev.pegasus.whatsappfilerecovery.utils.ConstantUtils.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream

/**
 * Created by: Sohaib Ahmed
 * Date: 7/14/2025
 * <p>
 * Links:
 * - LinkedIn: <a href="https://linkedin.com/in/epegasus">Linkedin</a>
 * - GitHub: <a href="https://github.com/epegasus">Github</a>
 */

class FileManager(private val context: Context, private val notificationManager: NotificationManager) {

    private val treeUri by lazy { (context.getSharedPreferences("permission_preferences", Context.MODE_PRIVATE)).getString("document_tree_uri", "")?.toUri() }

    suspend fun copyFile(srcDir: File, srcName: String) = withContext(Dispatchers.IO) {
        Log.v(TAG, "MediaObserverHigher: copyFile: ($srcDir, $srcName) -> $backupDir")
        checkForBackupFolder()
        val src = File(srcDir, srcName)
        val dst = File(backupDir, srcName)

        if (!src.exists()) {
            Log.e(TAG, "FileUtils: copySafely: Source doesn't exist. Src: $src")
            return@withContext
        }

        val isVoiceNote = srcDir.absolutePath.contains("Voice Notes", ignoreCase = true)
        val isScopedStorage = srcDir.absolutePath.contains("/Android/media/", ignoreCase = true)

        when (isVoiceNote && isScopedStorage) {
            true -> copyViaDocumentTree(src, dst)
            false -> simpleCopy(src, dst)
        }
    }

    suspend fun recoverFile(srcDir: File, srcName: String) = withContext(Dispatchers.IO) {
        Log.v(TAG, "MediaObserverHigher: recoverFile: ($srcDir, $srcName) -> $recoveryDir")
        checkForRecoverFolder()
        val src = File(backupDir, srcName)
        val dst = File(recoveryDir, srcName)

        if (!src.exists()) {
            Log.e(TAG, "FileUtils: recoverFile: Source doesn't exist. Src: $src")
            return@withContext
        }

        simpleCopy(src, dst) {
            src.delete()
            notificationManager.postNotificationRecovery(src)
        }
    }

    private fun simpleCopy(src: File, dst: File, postNotification: (() -> Unit)? = null) {
        runCatching {
            src.copyTo(dst, overwrite = true)
        }.onSuccess {
            Log.d(TAG, "FileManager: simpleCopy: Copied Successfully from: $src, to: $dst")
            postNotification?.invoke()
        }.onFailure {
            Log.e(TAG, "FileManager: simpleCopy: Failed to Copied from: $src, to: $dst", it)
        }
    }

    private fun copyViaDocumentTree(src: File, dst: File) {
        runCatching {
            val documentTreeUri = treeUri
            if (documentTreeUri == null) {
                throw NullPointerException("Tree Uri is null")
                return
            }

            val documentFile = DocumentFile.fromTreeUri(context, documentTreeUri)
            val voiceNotesDir = when (src.absolutePath.contains("com.whatsapp.w4b", ignoreCase = true)) {
                true -> documentFile
                    ?.findFile("com.whatsapp.w4b")
                    ?.findFile("WhatsApp Business")
                    ?.findFile("Media")
                    ?.findFile("WhatsApp Business Voice Notes")

                false -> documentFile
                    ?.findFile("com.whatsapp")
                    ?.findFile("WhatsApp")
                    ?.findFile("Media")
                    ?.findFile("WhatsApp Voice Notes")
            }
            voiceNotesDir?.let { voiceNotesDir ->
                voiceNotesDir
                    .listFiles()
                    .firstOrNull { it.name.equals(src.parentFile?.name) }
                    ?.listFiles()
                    ?.firstOrNull { it.name == src.name }
                    ?.let { srcDocumentFile ->
                        val parcelFileDescriptor = context.contentResolver.openFileDescriptor(srcDocumentFile.uri, "r", null)
                        val inputStream = FileInputStream(parcelFileDescriptor?.fileDescriptor)
                        val outputStream = FileOutputStream(dst)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            FileUtils.copy(inputStream, outputStream)
                            Log.d(TAG, "copyViaDocumentFile: copied")
                            return@runCatching
                        }
                    }
            }
            throw FileNotFoundException("File ($src) document uri not found")
        }.onSuccess {
            Log.d(TAG, "FileManager: copyViaDocumentTree: Copied Successfully from: $src, to: $dst")
        }.onFailure {
            Log.e(TAG, "FileManager: copyViaDocumentTree: Failed to Copied from: $src, to: $dst")
        }
    }

    private fun checkForBackupFolder() {
        if (!backupDir.exists()) backupDir.mkdirs()
    }

    private fun checkForRecoverFolder() {
        if (!recoveryDir.exists()) recoveryDir.mkdirs()
    }
}