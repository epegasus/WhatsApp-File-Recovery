package dev.pegasus.whatsappfilerecovery.utils

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.FileUtils
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import dev.pegasus.whatsappfilerecovery.utils.ConstantUtils.TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Created by: Sohaib Ahmed
 * Date: 6/30/2025
 * <p>
 * Links:
 * - LinkedIn: <a href="https://linkedin.com/in/epegasus">Linkedin</a>
 * - GitHub: <a href="https://github.com/epegasus">Github</a>
 */

suspend fun File.copySafely(context: Context, dstDir: File, treeUri: Uri) = withContext(Dispatchers.IO) {
    val src = this@copySafely
    runCatching {
        if (!dstDir.exists()) dstDir.mkdirs()
        if (!src.exists()) {
            Log.e(TAG, "FileUtils: copySafely: Source doesn't exist. Src: $src")
            return@runCatching
        }

        val newFile = File(dstDir, src.name)
        copyViaDocumentFile(context, src, dstDir, treeUri)
        /*when {
            src.canReadDirectly() -> {
                src.copyTo(newFile, overwrite = true)
                Log.d(TAG, "FileUtils: copySafely: Copied ${src.name} → $newFile")
            }
            else -> copyViaDocumentFile(context, src, dstDir, treeUri)
        }*/
    }.onFailure { Log.e(TAG, "FileUtils: copySafely: Copy failed: ${src.path}", it) }
}

fun File.canReadDirectly(): Boolean {
    val mediaRoot = Environment.getExternalStorageDirectory().absolutePath + "/Android/media/"

    // Define the known "can't read directly" folders
    val voiceNoteDirs = listOf(
        "$mediaRoot/com.whatsapp.w4b/WhatsApp Business/Media/WhatsApp Business Voice Notes",
        "$mediaRoot/com.whatsapp/WhatsApp/Media/WhatsApp Voice Notes",
        "$mediaRoot/com.whatsapp/WhatsApp/Media/Whatsapp Images",
    )

    val isVoiceNoteDir = voiceNoteDirs.any { path.startsWith(it) }

    return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || (!path.startsWith(mediaRoot) || !isVoiceNoteDir)
}

private fun copyViaDocumentFile(context: Context, src: File, dstDir: File, treeUri: Uri) {
    CoroutineScope(Dispatchers.Default).launch {
        Log.d(TAG, "copyViaDocumentFile: called")
        DocumentFile.fromTreeUri(context, treeUri)
            ?.findFile("com.whatsapp")
            ?.findFile("WhatsApp")
            ?.findFile("Media")
            ?.findFile("WhatsApp Voice Notes")
            ?.let { voiceNotesDir ->
                voiceNotesDir
                    .listFiles()
                    .firstOrNull { it.name.equals(src.parentFile?.name) }
                    ?.listFiles()
                    ?.firstOrNull { it.name == src.name }
                    ?.let { srcIt ->
                        val dstFile = srcIt.name?.let { it1 -> File(dstDir, it1) }
                        val parcelFileDescriptor = context.contentResolver.openFileDescriptor(srcIt.uri, "r", null)
                        val inputStream = FileInputStream(parcelFileDescriptor?.fileDescriptor)
                        val outputStream = FileOutputStream(dstFile)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            FileUtils.copy(inputStream, outputStream)
                            Log.d(TAG, "copyViaDocumentFile: copied")
                            return@launch
                        }
                    }
            }
        Log.e(TAG, "copyViaDocumentFile: failed to copy")
    }
}