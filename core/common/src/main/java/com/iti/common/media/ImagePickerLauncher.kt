package com.iti.common.media

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class ImagePickerLauncher(
    private val onLaunchGallery: () -> Unit,
    private val onLaunchCamera: (Uri) -> Unit,
    private val onRequestCameraPermission: () -> Unit
) {
    fun launchGallery() {
        onLaunchGallery()
    }

    fun launchCamera(uri: Uri) {
        onLaunchCamera(uri)
    }

    fun requestCameraPermission() {
        onRequestCameraPermission()
    }
}

@Composable
fun rememberImagePickerLauncher(
    onImagePicked: (Uri) -> Unit,
    onCameraPermissionGranted: () -> Unit,
    onCameraPermissionDenied: () -> Unit
): ImagePickerLauncher {
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> 
        uri?.let(onImagePicked) 
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingCameraUri?.let(onImagePicked)
        }
        pendingCameraUri = null
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onCameraPermissionGranted()
        } else {
            onCameraPermissionDenied()
        }
    }

    return remember(galleryLauncher, cameraLauncher, cameraPermissionLauncher) {
        ImagePickerLauncher(
            onLaunchGallery = {
                galleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onLaunchCamera = { uri ->
                pendingCameraUri = uri
                cameraLauncher.launch(uri)
            },
            onRequestCameraPermission = {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        )
    }
}
