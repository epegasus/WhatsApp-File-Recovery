package dev.pegasus.whatsappfilerecovery.utils

import java.io.File

/**
 * Created by: Sohaib Ahmed
 * Date: 7/14/2025
 * <p>
 * Links:
 * - LinkedIn: <a href="https://linkedin.com/in/epegasus">Linkedin</a>
 * - GitHub: <a href="https://github.com/epegasus">Github</a>
 */

object ConfigUtils {

    lateinit var backupRoot: File
    lateinit var recoveryRoot: File

    fun initPaths(baseDir: File) {
        backupRoot = File(baseDir, "backup_files").apply { mkdirs() }
        recoveryRoot = File(baseDir, "recovery_files").apply { mkdirs() }
    }

    fun getBackupDir(isBusiness: Boolean, type: MediaType): File {
        val root = if (isBusiness) File(backupRoot, "business_whatsapp") else File(backupRoot, "whatsapp")
        return File(root, type.folderName).apply { mkdirs() }
    }

    fun getRecoveryDir(isBusiness: Boolean, type: MediaType): File {
        val root = if (isBusiness) File(recoveryRoot, "business_whatsapp") else File(recoveryRoot, "whatsapp")
        return File(root, type.folderName).apply { mkdirs() }
    }

    enum class MediaType(val folderName: String) {
        IMAGES("images"),
        VIDEOS("videos"),
        GIFS("gifs"),
        STICKERS("stickers"),
        AUDIO("audio"),
        VOICE_NOTES("voice_notes"),
        DOCUMENTS("documents");

        companion object {
            fun fromPath(path: String): MediaType? {
                return when {
                    path.contains("images", true) -> IMAGES
                    path.contains("video", true) -> VIDEOS
                    path.contains("gif", true) -> GIFS
                    path.contains("sticker", true) -> STICKERS
                    path.contains("audio", true) -> AUDIO
                    path.contains("voice", true) -> VOICE_NOTES
                    path.contains("document", true) -> DOCUMENTS
                    else -> null
                }
            }
        }
    }
}