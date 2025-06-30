package dev.pegasus.whatsappfilerecovery.utils

import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.documentfile.provider.DocumentFile
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

suspend fun File.copySafely(context: Context, dstDir: File) = withContext(Dispatchers.IO) {
    val src = this@copySafely
    runCatching {
        if (!dstDir.exists()) dstDir.mkdirs()
        if (!src.exists()) {
            Log.e(TAG, "FileUtils: copySafely: Source doesn't exist. Src: $src")
            return@runCatching
        }

        when {
            src.canReadDirectly() -> src.copyTo(File(dstDir, src.name), overwrite = true)
            else -> copyViaDocumentFile(context, src, dstDir)   // Scoped‑storage fallback
        }
        Log.d(TAG, "FileUtils: copySafely: Copied ${src.name} → $dstDir")
    }.onFailure { Log.e(TAG, "FileUtils: copySafely: Copy failed: ${src.path}", it) }
}

fun File.canReadDirectly(): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
            || !path.startsWith("${Environment.getExternalStorageDirectory()}/Android/media/")
}

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
}
