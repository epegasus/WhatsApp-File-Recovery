package dev.pegasus.whatsappfilerecovery.activities

import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import dev.pegasus.whatsappfilerecovery.adapters.AdapterMedia
import dev.pegasus.whatsappfilerecovery.base.BaseActivity
import dev.pegasus.whatsappfilerecovery.databinding.ActivityMediaBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ActivityMedia : BaseActivity<ActivityMediaBinding>(ActivityMediaBinding::inflate) {

    private val adapter by lazy { AdapterMedia() }

    private val caseType by lazy { intent.getIntExtra("caseType", 0) }
    private val recoveryDir by lazy { File(filesDir, "file_recovery") }

    override fun onCreated() {
        initRecyclerView()
        fetchData()

        binding.toolbarMedia.setNavigationOnClickListener { finish() }
    }

    private fun initRecyclerView() {
        binding.rcvList.adapter = adapter
    }

    private fun fetchData() = when (caseType) {
        TYPE_IMAGES -> fetchImages()
        TYPE_VIDEOS -> fetchVideos()
        TYPE_AUDIO -> fetchAudio()
        TYPE_DOCS -> fetchDocuments()
        else -> fetchImages()
    }

    private fun fetchImages() = lifecycleScope.launch {
        val imageExtensions = setOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "heic", "heif")
        val imageFiles = withContext(Dispatchers.IO) {
            recoveryDir
                .walkTopDown()
                .filter { it.isFile && it.extension.lowercase() in imageExtensions }
                .toList()
        }
        binding.progressBar.isVisible = false
        binding.mtvEmpty.isVisible = imageFiles.isEmpty()
        adapter.submitList(imageFiles.map { it.path })
    }

    /* ---- Stubs for other types (fill in later) ---- */

    private fun fetchVideos() {}

    private fun fetchAudio() {}

    private fun fetchDocuments() {}

    // Handy constants
    companion object {
        const val KEY_CASE_TYPE = "caseType"
        const val TYPE_IMAGES = 0
        const val TYPE_VIDEOS = 1
        const val TYPE_AUDIO = 2
        const val TYPE_DOCS = 3
    }
}