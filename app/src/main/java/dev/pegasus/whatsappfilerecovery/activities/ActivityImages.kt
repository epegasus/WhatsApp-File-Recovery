package dev.pegasus.whatsappfilerecovery.activities

import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import dev.pegasus.whatsappfilerecovery.adapters.AdapterImages
import dev.pegasus.whatsappfilerecovery.base.BaseActivity
import dev.pegasus.whatsappfilerecovery.databinding.ActivityMediaBinding
import dev.pegasus.whatsappfilerecovery.utils.ConfigUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ActivityImages : BaseActivity<ActivityMediaBinding>(ActivityMediaBinding::inflate) {

    private val adapter by lazy { AdapterImages() }

    override fun onCreated() {
        initRecyclerView()
        fetchImages()

        binding.toolbarMedia.setNavigationOnClickListener { finish() }
    }

    private fun initRecyclerView() {
        binding.rcvList.adapter = adapter
    }

    private fun fetchImages() = lifecycleScope.launch {
        val imageExtensions = setOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "heic", "heif")
        val imageFiles = withContext(Dispatchers.IO) {
            ConfigUtils
                .recoveryDir
                .walkTopDown()
                .filter { it.isFile && it.extension.lowercase() in imageExtensions }
                .toList()
        }
        binding.progressBar.isVisible = false
        binding.mtvEmpty.isVisible = imageFiles.isEmpty()
        adapter.submitList(imageFiles.map { it.path })
    }
}