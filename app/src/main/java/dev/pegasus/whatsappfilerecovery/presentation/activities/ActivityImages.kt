package dev.pegasus.whatsappfilerecovery.presentation.activities

import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import dev.pegasus.whatsappfilerecovery.data.dataSource.DataSourceMedia
import dev.pegasus.whatsappfilerecovery.databinding.ActivityImagesBinding
import dev.pegasus.whatsappfilerecovery.presentation.adapters.AdapterImages
import dev.pegasus.whatsappfilerecovery.utils.base.BaseActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ActivityImages : BaseActivity<ActivityImagesBinding>(ActivityImagesBinding::inflate) {

    private val adapter by lazy { AdapterImages() }

    override fun onCreated() {
        initRecyclerView()
        fetchData()

        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun initRecyclerView() {
        binding.rcvList.adapter = adapter
    }

    private fun fetchData() = lifecycleScope.launch(Dispatchers.IO) {
        val dataSource = DataSourceMedia()
        val whatsAppMedia = dataSource.getPhotos(0)
        val businessWhatsAppMedia = dataSource.getPhotos(1)
        val completeList = whatsAppMedia + businessWhatsAppMedia

        withContext(Dispatchers.Main) {
            binding.progressBar.isVisible = false
            binding.mtvEmpty.isVisible = completeList.isEmpty()
            adapter.submitList(completeList.map { it.filePath })
        }
    }
}