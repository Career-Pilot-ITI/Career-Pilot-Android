package com.iti.careerpilot.editprofile.presentation.screen.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.core.designsystem.CareerPilotShapes
import com.iti.careerpilot.editprofile.R

@Composable
fun CVUploadField(
    fileName: String,
    isUploading: Boolean,
    uploadProgress: Int,
    onCVSelected: (Uri?) -> Unit,
    onCVRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> onCVSelected(uri) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.upload_cv),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        when {
            isUploading -> CVUploadingState(uploadProgress)
            fileName.isNotBlank() -> CVUploadedState(
                fileName = fileName,
                onChange = { launcher.launch("application/pdf") },
                onRemove = onCVRemove
            )
            else -> CVEmptyState(onClick = { launcher.launch("application/pdf") })
        }
    }
}

@Composable
private fun CVEmptyState(onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(CareerPilotShapes.medium)
            .dashedBorder(
                color = MaterialTheme.colorScheme.outline,
                shape = CareerPilotShapes.medium
            )
            .clickable(onClick = onClick)
            .padding(vertical = 24.dp, horizontal = 16.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_upload),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.no_cv_uploaded),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = stringResource(R.string.tap_to_upload_pdf),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CVUploadedState(
    fileName: String,
    onChange: () -> Unit,
    onRemove: () -> Unit
) {
    val success = MaterialTheme.colorScheme.tertiary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CareerPilotShapes.medium)
            .background(success.copy(alpha = 0.08f))
            .border(1.dp, success.copy(alpha = 0.3f), CareerPilotShapes.medium)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(success.copy(alpha = 0.15f))
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_docs),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = success
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = fileName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_check),
                    contentDescription = null,
                    tint = success,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.cv_uploaded),
                    style = MaterialTheme.typography.labelSmall,
                    color = success
                )
            }
        }

        IconButton(onClick = onChange, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_edit),
                contentDescription = stringResource(R.string.change_cv),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
        IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_close),
                contentDescription = stringResource(R.string.remove_cv),
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun CVUploadingState(uploadProgress: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CareerPilotShapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), CareerPilotShapes.medium)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CircularProgressIndicator(
            progress = { uploadProgress / 100f },
            modifier = Modifier.size(32.dp),
            strokeWidth = 3.dp,
            color = MaterialTheme.colorScheme.primary
        )
        Column {
            Text(
                text = stringResource(R.string.uploading),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.uploading_cv, uploadProgress),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun Modifier.dashedBorder(
    color: Color,
    shape: Shape,
    strokeWidth: Dp = 1.5.dp,
    dashLength: Dp = 6.dp,
    gapLength: Dp = 4.dp
) = this.drawWithContent {
    drawContent()
    val outline = shape.createOutline(size, layoutDirection, this)
    drawOutline(
        outline = outline,
        color = color,
        style = Stroke(
            width = strokeWidth.toPx(),
            pathEffect = PathEffect.dashPathEffect(
                floatArrayOf(dashLength.toPx(), gapLength.toPx())
            )
        )
    )
}