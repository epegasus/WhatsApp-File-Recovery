package dev.pegasus.whatsappfilerecovery.data.dataSource

import android.media.MediaMetadataRetriever
import dev.pegasus.whatsappfilerecovery.data.dataClasses.ItemMedia
import dev.pegasus.whatsappfilerecovery.utils.ConfigUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.log10
import kotlin.math.pow

/**
 * Created by: Sohaib Ahmed
 * Date: 7/30/2025
 * <p>
 * Links:
 * - LinkedIn: <a href="https://linkedin.com/in/epegasus">Linkedin</a>
 * - GitHub: <a href="https://github.com/epegasus">Github</a>
 */

class DataSourceMedia {

    /**
     *  @param caseType
     *      0: WhatsApp
     *      1: Business WhatsApp
     */
    fun getPhotos(caseType: Int): List<ItemMedia> =
        getMedia(caseType, ConfigUtils.MediaType.IMAGES, listOf("jpg", "jpeg", "png", "bmp", "webp"), fileType = 1)

    fun getVideos(caseType: Int): List<ItemMedia> =
        getMedia(caseType, ConfigUtils.MediaType.VIDEOS, listOf("mp4", "mkv", "3gp", "webm"), fileType = 2)

    fun getGifs(caseType: Int): List<ItemMedia> =
        getMedia(caseType, ConfigUtils.MediaType.GIFS, listOf("mp4", "gif"), fileType = 3)

    fun getStickers(caseType: Int): List<ItemMedia> =
        getMedia(caseType, ConfigUtils.MediaType.STICKERS, listOf("webp"), fileType = 4, isSticker = true)

    fun getAudios(caseType: Int): List<ItemMedia> =
        getMedia(caseType, ConfigUtils.MediaType.AUDIO, listOf("mp3", "wav", "aac", "m4a", "opus"), fileType = 5)

    fun getVoices(caseType: Int): List<ItemMedia> =
        getMedia(caseType, ConfigUtils.MediaType.VOICE_NOTES, listOf("opus", "ogg", "m4a"), fileType = 6)

    fun getDocuments(caseType: Int): List<ItemMedia> =
        getMedia(caseType, ConfigUtils.MediaType.DOCUMENTS, listOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "zip", "rar"), fileType = 7)

    private fun getMedia(
        caseType: Int,
        mediaType: ConfigUtils.MediaType,
        extensions: List<String>,
        fileType: Int,
        isSticker: Boolean = false
    ): List<ItemMedia> {
        val isBusiness = caseType == 1
        val targetDir = ConfigUtils.getRecoveryDir(isBusiness, mediaType)

        if (!targetDir.exists()) return emptyList()

        return targetDir
            .listFiles { file ->
                file.isFile &&
                        extensions.any { file.extension.equals(it, ignoreCase = true) } &&
                        (!isSticker || file.name.contains("sticker", ignoreCase = true))
            }
            ?.sortedByDescending { it.lastModified() }
            ?.mapIndexed { index, file ->
                val readableSize = getReadableFileSize(file.length())
                val readableDate = getFormattedDate(file.lastModified())
                val readableDuration = when (fileType) {
                    1, 2 -> getMediaDurationFormatted(file.absolutePath) // Only video & audio
                    else -> null
                }

                ItemMedia(
                    id = index,
                    title = file.nameWithoutExtension,
                    filePath = file.absolutePath,
                    fileType = fileType,
                    fileSize = file.length(),
                    fileDate = file.lastModified(),
                    fileSizeReadable = readableSize,
                    fileDurationReadable = readableDuration,
                    fileDateReadable = readableDate
                )
            } ?: emptyList()
    }

    private fun getReadableFileSize(size: Long): String {
        if (size <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        return String.format(Locale.ENGLISH, "%.1f %s", size / 1024.0.pow(digitGroups.toDouble()), units[digitGroups])
    }

    private fun getFormattedDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    private fun getMediaDurationFormatted(filePath: String): String? {
        return try {
            val retriever = MediaMetadataRetriever().apply { setDataSource(filePath) }
            val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
            retriever.release()
            durationMs?.let {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(it)
                val seconds = TimeUnit.MILLISECONDS.toSeconds(it) % 60
                String.format(Locale.ENGLISH, "%02d:%02d", minutes, seconds)
            }
        } catch (e: Exception) {
            null
        }
    }
}