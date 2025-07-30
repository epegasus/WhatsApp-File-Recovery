package dev.pegasus.whatsappfilerecovery.data.dataClasses

/**
 * Created by: Sohaib Ahmed
 * Date: 7/30/2025
 * <p>
 * Links:
 * - LinkedIn: <a href="https://linkedin.com/in/epegasus">Linkedin</a>
 * - GitHub: <a href="https://github.com/epegasus">Github</a>
 */


/**
 *  @property: fileType
 *      1: Image
 *      2: Video
 *      3: Gif
 *      4: Sticker
 *      5: Audio
 *      6: Voice Notes
 *      7: Document
 */
data class ItemMedia(
    val id: Int,
    val title: String,
    val filePath: String,
    val fileType: Int,
    val fileSize: Long,
    val fileDate: Long,
    val fileSizeReadable: String?,
    val fileDurationReadable: String?,
    val fileDateReadable: String?,
)