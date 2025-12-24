package com.profgroep8.rmc_app.utils

import PhotoUtils
import android.content.Context
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

class PhotoPermissionHandler(
    private val context: Context,
    private val onPermissionsResult: (Boolean) -> Unit
) {
    fun requestPermissions(launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>) {
        launcher.launch(PhotoUtils.getRequiredPermissions())
    }

    fun hasPermissions(): Boolean {
        return PhotoUtils.hasAllPermissions(context)
    }
}

@Composable
fun rememberPhotoPermissionLauncher(
    onPermissionsResult: (Boolean) -> Unit
): ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>> {
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allRequiredGranted = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val hasCamera = permissions[android.Manifest.permission.CAMERA] == true
            val hasFullImageAccess = permissions[android.Manifest.permission.READ_MEDIA_IMAGES] == true
            val hasSelectedAccess = permissions[android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED] == true
            hasCamera && (hasFullImageAccess || hasSelectedAccess)
        } else {
            permissions.values.all { it }
        }
        onPermissionsResult(allRequiredGranted)
    }
}