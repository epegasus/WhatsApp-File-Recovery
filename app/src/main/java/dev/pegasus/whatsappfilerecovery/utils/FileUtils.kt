package dev.pegasus.whatsappfilerecovery.utils

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import dev.pegasus.whatsappfilerecovery.utils.ConstantUtils.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
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
        when {
            src.canReadDirectly() -> src.copyTo(newFile, overwrite = true)
            else -> copyViaDocumentFile(context, src, dstDir, treeUri)   // Scoped‑storage fallback
        }
        Log.d(TAG, "FileUtils: copySafely: Copied ${src.name} → $newFile")
    }.onFailure { Log.e(TAG, "FileUtils: copySafely: Copy failed: ${src.path}", it) }
}

fun File.canReadDirectly(): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
            || !path.startsWith("${Environment.getExternalStorageDirectory()}/Android/media/")
}

private fun copyViaDocumentFile(
    context: Context,
    src: File,
    dst: File, // Only used for its file name
    treeUri: Uri
) {
    val contentResolver = context.contentResolver

    // Convert file path to URI (content URI) using the contentResolver
    val contentUri = getContentUriFromFile(context, src)

    contentUri?.let { uri ->
        val inputStream = contentResolver.openInputStream(uri)

        inputStream?.let { input ->
            // Define destination file in your app's cache directory
            val destinationFile = File(dst, src.name)
            val outputStream = FileOutputStream(destinationFile)

            try {
                input.copyTo(outputStream)
                // Successfully copied to cache dir
                Log.d(TAG, "File copied to cache: ${destinationFile.absolutePath}")
            } catch (e: Exception) {
                Log.e(TAG, "Error copying file", e)
            } finally {
                input.close()
                outputStream.close()
            }
        }
    } ?: run {
        Log.e(TAG, "Invalid content URI or permission issues")
    }
}

// Function to get content URI from file path (SAF)
private fun getContentUriFromFile(context: Context, file: File): Uri? {
    val uri = Uri.fromFile(file)
    val contentUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // Use MediaStore to get a content URI for Android 10 and above
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val selection = "${MediaStore.Images.Media.DATA} = ?"
        val selectionArgs = arrayOf(file.absolutePath)
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        context.contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                return ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            }
        }
        null
    } else {
        // For older Android versions, simply use the File URI
        uri
    }
    return contentUri
}


/*
private fun copyViaDocumentFile(context: Context, src: File, dstDir: File) {
    findPersistedTree(context, src) ?: return
    val doc = DocumentFile.fromFile(src)
    val input = context.contentResolver.openInputStream(doc.uri) ?: return
    val outFile = File(dstDir, src.name)
    input.use { inp -> FileOutputStream(outFile).use { inp.copyTo(it) } }
}

private fun findPersistedTree(context: Context, src: File): DocumentFile? {
    return context
        .contentResolver
        .persistedUriPermissions
        .firstOrNull {
            src
                .path
                .startsWith(it.uri.path.orEmpty())
        }?.let {
            DocumentFile.fromTreeUri(context, it.uri)
        }
}*/
