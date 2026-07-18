package com.iti.careerpilot.editprofile.presentation.screen.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue.Expanded
import androidx.compose.material3.SheetValue.Hidden
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil3.compose.AsyncImage
import com.iti.careerpilot.core.designsystem.CareerPilotShapes
import com.iti.careerpilot.core.designsystem.common.PermissionsDialog
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
import com.iti.careerpilot.editprofile.R
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarSourcePickerSheet(
    onDismiss: () -> Unit,
    onImagePicked: (Uri?) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberBottomSheetState(
        initialValue = Hidden,
        enabledValues = setOf(Hidden, Expanded)
    )

    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var showCameraPermissionDialog by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> onImagePicked(uri) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success -> onImagePicked(if (success) pendingCameraUri else null) }

    fun launchCamera() {
        val uri = createCameraOutputUri(context)
        pendingCameraUri = uri
        cameraLauncher.launch(uri)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            Text(
                text = stringResource(R.string.update_profile_photo),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            AvatarSourceOption(
                icon = ImageVector.vectorResource(R.drawable.ic_photo),
                label = stringResource(R.string.choose_from_gallery),
                onClick = {
                    galleryLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            )
            AvatarSourceOption(
                icon = ImageVector.vectorResource(R.drawable.ic_camera),
                label = stringResource(R.string.take_a_photo),
                onClick = {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasPermission) launchCamera() else showCameraPermissionDialog = true
                }
            )

            Spacer(Modifier.height(12.dp))
        }
    }

    if (showCameraPermissionDialog) {
        PermissionsDialog(
            title = stringResource(R.string.camera_permission_title),
            text = stringResource(R.string.camera_permission_text),
            icon = ImageVector.vectorResource(R.drawable.ic_camera),
            cancel = stringResource(R.string.cancel),
            allow = stringResource(R.string.allow),
            neededPermissions = arrayOf(Manifest.permission.CAMERA),
            onDismiss = { showCameraPermissionDialog = false },
            onGranted = { launchCamera() }
        )
    }
}

@Composable
fun AvatarSourceOption(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(CareerPilotShapes.medium)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

fun createCameraOutputUri(context: Context): Uri {
    val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
    val file = File(imagesDir, "avatar_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}