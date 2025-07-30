package dev.pegasus.whatsappfilerecovery

import android.Manifest
import android.app.ActivityManager
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import dev.pegasus.whatsappfilerecovery.data.services.MediaService
import dev.pegasus.whatsappfilerecovery.databinding.ActivityMainBinding
import dev.pegasus.whatsappfilerecovery.presentation.activities.ActivityImages
import dev.pegasus.whatsappfilerecovery.presentation.activities.ActivityVideos
import dev.pegasus.whatsappfilerecovery.utils.ConfigUtils
import dev.pegasus.whatsappfilerecovery.utils.base.BaseActivity

class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    private val serviceIntent by lazy { Intent(this, MediaService::class.java) }
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        when (it) {
            true -> toggleService()
            false -> Toast.makeText(this, "Permission Required", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreated() {
        setPath()
        setUI()

        binding.mbImage.setOnClickListener { startActivity(Intent(this, ActivityImages::class.java)) }
        binding.mbVideo.setOnClickListener { startActivity(Intent(this, ActivityVideos::class.java)) }
        binding.mbAudio.setOnClickListener { Toast.makeText(this, "Under Development", Toast.LENGTH_SHORT).show() }
        binding.mbDocument.setOnClickListener { Toast.makeText(this, "Under Development", Toast.LENGTH_SHORT).show() }
        binding.mbService.setOnClickListener { askPermission() }
    }

    private fun setPath() {
        ConfigUtils.initPaths(filesDir)
    }

    private fun setUI() {
        when (isServiceRunning()) {
            true -> binding.mbService.setText(R.string.stop_service)
            false -> binding.mbService.setText(R.string.start_service)
        }
    }

    private fun askPermission() {
        askFullStoragePermission {
            when (it) {
                true -> askMediaPermission { mediaPermission ->
                    when (mediaPermission) {
                        true -> askNotificationPermission()
                        false -> Toast.makeText(this, "Permission Required", Toast.LENGTH_SHORT).show()
                    }
                }

                false -> Toast.makeText(this, "Permission Required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }
        toggleService()
    }

    private fun toggleService() {
        when (isServiceRunning()) {
            true -> stopService(serviceIntent)
            false -> ContextCompat.startForegroundService(this, serviceIntent)
        }
        setUI()
    }

    private fun isServiceRunning(): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        @Suppress("DEPRECATION") // Since API 26 the list is limited to your own app (perfect for us).
        return manager.getRunningServices(Int.MAX_VALUE).any { it.service.className == MediaService::class.java.name }
    }
}