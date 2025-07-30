package dev.pegasus.whatsappfilerecovery.presentation.activities

import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import dev.pegasus.whatsappfilerecovery.data.dataSource.DataSourceMedia
import dev.pegasus.whatsappfilerecovery.databinding.ActivityMediaBinding
import dev.pegasus.whatsappfilerecovery.presentation.adapters.AdapterImages
import dev.pegasus.whatsappfilerecovery.utils.base.BaseActivity
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

    private fun fetchImages() = lifecycleScope.launch(Dispatchers.IO) {
        val dataSource = DataSourceMedia()
        val whatsAppImages = dataSource.getPhotos(0)
        val businessWhatsAppImages = dataSource.getPhotos(1)
        val totalImages = whatsAppImages + businessWhatsAppImages

        withContext(Dispatchers.Main) {
            binding.progressBar.isVisible = false
            binding.mtvEmpty.isVisible = totalImages.isEmpty()
            adapter.submitList(totalImages.map { it.filePath })
        }
    }
}