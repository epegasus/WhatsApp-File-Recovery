package dev.pegasus.whatsappfilerecovery.utils.base

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.pegasus.whatsappfilerecovery.R
import dev.pegasus.whatsappfilerecovery.utils.ConstantUtils.TAG
import dev.pegasus.whatsappfilerecovery.utils.hasR
import dev.pegasus.whatsappfilerecovery.utils.hasT
import dev.pegasus.whatsappfilerecovery.utils.hasU

/**
 * Created by: Sohaib Ahmed
 * Date: 6/30/2025
 * <p>
 * Links:
 * - LinkedIn: <a href="https://linkedin.com/in/epegasus">Linkedin</a>
 * - GitHub: <a href="https://github.com/epegasus">Github</a>
 */

open class BaseActivityPermission : AppCompatActivity() {

    private val sharedPreferences by lazy { getSharedPreferences("permission_preferences", MODE_PRIVATE) }
    private val editor by lazy { sharedPreferences?.edit() }
    private var callback: ((Boolean) -> Unit)? = null

    private val storagePermission by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }

    private val storagePermissionArray by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        val contains = it.values.contains(true)
        callback?.invoke(contains)
    }

    private var settingStorageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
        val permissionResult = checkStoragePermission()
        callback?.invoke(permissionResult)
    }

    private var settingDocumentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val uri = handleDocumentTreePermissionResult(result.data)
        val permissionResult = uri != null
        callback?.invoke(permissionResult)
    }

    fun checkMediaPermission(): Boolean {
        return if (hasR()) {
            val savedUri = getSavedDocumentUri()
            if (getSavedDocumentUri().isNullOrEmpty()) {
                return false
            }
            try {
                val treeUri = savedUri?.toUri() ?: return false
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                contentResolver.takePersistableUriPermission(treeUri, takeFlags)
                true
            } catch (ex: Exception) {
                Log.e(TAG, "BasePermissionFragment: checkDocumentTreePermission: ", ex)
                false
            }
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun askMediaPermission(callback: (Boolean) -> Unit) {
        this.callback = callback
        if (checkMediaPermission()) {
            // Check permission again
            callback.invoke(true)
        } else {
            // Ask permission
            if (hasR()) {
                askDocumentTreeAccess()
            } else {
                askStoragePermission()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun askDocumentTreeAccess() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, "content://com.android.externalstorage.documents/tree/primary:Android%2Fmedia".toUri())
            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            )
        }
        settingDocumentLauncher.launch(intent)
    }

    private fun handleDocumentTreePermissionResult(data: Intent?): Uri? {
        val uri = data?.data ?: return null
        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        saveDocumentUri(uri.toString())
        return uri
    }

    fun checkStoragePermission(): Boolean {
        return if (
            hasT()
            && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        ) {
            // Full access on Android 13 (API level 33) or higher
            true
        } else if (
            hasU()
            && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) == PackageManager.PERMISSION_GRANTED
        ) {
            // Partial access on Android 14 (API level 34) or higher
            true
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            true
        } else {
            false
        }
    }

    /**
     * Full access on Android 14 (API level 34) or higher
     */
    fun checkFullStoragePermission(): Boolean? {
        return if (hasU()) {
            // Check if "Allow limited access" is selected
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                true // Full access granted
            } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) == PackageManager.PERMISSION_GRANTED) {
                false // Partial access granted
            } else {
                null // No permission granted
            }
        } else {
            true // On devices below Android 14, assume full access by default
        }
    }

    fun askStoragePermission(callback: (Boolean) -> Unit) {
        this.callback = callback
        if (checkStoragePermission()) {
            callback.invoke(true)
        } else {
            askStoragePermission()
        }
    }

    fun askFullStoragePermission(callback: (Boolean) -> Unit) {
        this.callback = callback
        if (checkFullStoragePermission() == true) {
            callback.invoke(true)
        } else {
            askStoragePermission()
        }
    }

    /* --------------------------- Permission smaller than api R (android: 30 {11}) & Higher (for saving only) --------------------------- */

    private fun askStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, storagePermission)) {
            showPermissionDialog(
                title = R.string.dialog_permission_storage_title,
                message = R.string.dialog_permission_storage_message,
                permissionArray = storagePermissionArray,
            )
        } else {
            if (sharedPreferences?.getBoolean(storagePermission, true) == true) {
                editor?.putBoolean(storagePermission, false)
                editor?.apply()
                permissionLauncher.launch(storagePermissionArray)
            } else {
                showSettingDialog()
            }
        }
    }

    private fun showPermissionDialog(title: Int, message: Int, permissionArray: Array<String>) {
        val builder = MaterialAlertDialogBuilder(this).also {
            it.setTitle(title)
            it.setMessage(message)
            it.setPositiveButton(R.string.allow) { dialog, _ ->
                dialog.dismiss()
                permissionLauncher.launch(permissionArray)
            }
            it.setNegativeButton(R.string.deny) { dialog, _ ->
                dialog.dismiss()
                callback?.invoke(false)
            }
        }
        builder.show()
    }

    private fun showSettingDialog() {
        val builder = MaterialAlertDialogBuilder(this).also {
            it.setTitle(R.string.dialog_permission_setting_title)
            it.setMessage(R.string.dialog_permission_setting_message)
            it.setPositiveButton(R.string.allow) { dialog, _ ->
                dialog.dismiss()
                openSettingPage()
            }
            it.setNegativeButton(R.string.deny) { dialog, _ ->
                dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun openSettingPage() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        settingStorageLauncher.launch(intent)
    }

    /* ----------------------------------------------- SharedPreferences ----------------------------------------------- */

    fun getSavedDocumentUri(): String? {
        return sharedPreferences?.getString("document_tree_uri", null)
    }

    private fun saveDocumentUri(uri: String) {
        editor?.putString("document_tree_uri", uri)?.apply()
    }
}