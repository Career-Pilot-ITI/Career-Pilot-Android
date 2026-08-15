package com.iti.careerpilot.core.designsystem.components

import android.text.format.Formatter
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.core.designsystem.R

enum class CvUploadCardStage {
    EMPTY,
    PREPARING,
    SELECTED,
    UPLOADING,
    PARSING,
    UPLOADED,
}

@Composable
fun CvUploadCard(
    fileName: String?,
    fileSizeBytes: Long,
    stage: CvUploadCardStage,
    uploadProgress: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(190.dp)
            .background(colors.surface, MaterialTheme.shapes.large)
            .clip(RoundedCornerShape(20.dp))
            .dashedRoundedBorder(
                color = if (stage == CvUploadCardStage.UPLOADED) colors.primary else colors.outline,
                cornerRadius = 20.dp,
            )
            .clickable(
                enabled = stage != CvUploadCardStage.PREPARING &&
                    stage != CvUploadCardStage.UPLOADING &&
                    stage != CvUploadCardStage.PARSING,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        when (stage) {
            CvUploadCardStage.EMPTY -> EmptyCvContent()
            CvUploadCardStage.PREPARING -> PreparingCvContent()
            CvUploadCardStage.SELECTED -> FileDetails(
                fileName = fileName,
                fileSizeBytes = fileSizeBytes,
                statusText = stringResource(R.string.cv_upload_ready),
                icon = { FileUploadIcon() },
            )
            CvUploadCardStage.UPLOADING,
            CvUploadCardStage.PARSING -> UploadingCvContent(
                fileName = fileName,
                fileSizeBytes = fileSizeBytes,
                progress = (uploadProgress / 100f).coerceIn(0f, 1f),
                isParsing = stage == CvUploadCardStage.PARSING || uploadProgress >= 100,
            )
            CvUploadCardStage.UPLOADED -> FileDetails(
                fileName = fileName,
                fileSizeBytes = fileSizeBytes,
                statusText = stringResource(R.string.cv_upload_uploaded),
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = colors.secondary,
                        modifier = Modifier.size(48.dp),
                    )
                },
            )
        }
    }
}

@Composable
private fun EmptyCvContent() {
    val colors = MaterialTheme.colorScheme
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(colors.surfaceVariant, MaterialTheme.shapes.medium),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.FileUpload,
                contentDescription = null,
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(28.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.cv_upload_tap_to_upload),
            style = MaterialTheme.typography.titleMedium,
            color = colors.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.cv_upload_requirements),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PreparingCvContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        LoadingIndicator(color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.cv_upload_preparing),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun UploadingCvContent(
    fileName: String?,
    fileSizeBytes: Long,
    progress: Float,
    isParsing: Boolean,
) {
    val statusText = if (isParsing || progress >= 1f) {
        stringResource(R.string.cv_upload_parsing)
    } else {
        stringResource(R.string.cv_upload_progress, (progress * 100).toInt())
    }
    FileDetails(
        fileName = fileName,
        fileSizeBytes = fileSizeBytes,
        statusText = statusText,
        icon = {
            LoadingIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp),
            )
        },
    )
}

@Composable
private fun FileDetails(
    fileName: String?,
    fileSizeBytes: Long,
    statusText: String,
    icon: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val detailsText = if (fileSizeBytes > 0L) {
        stringResource(
            R.string.cv_upload_file_details,
            Formatter.formatShortFileSize(context, fileSizeBytes),
            statusText,
        )
    } else {
        statusText
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        icon()
        Spacer(Modifier.height(16.dp))
        Text(
            text = fileName.orEmpty(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = detailsText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun FileUploadIcon() {
    Icon(
        imageVector = Icons.Outlined.FileUpload,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(42.dp),
    )
}

private fun Modifier.dashedRoundedBorder(
    color: Color,
    cornerRadius: Dp,
    strokeWidth: Dp = 1.dp,
    dashLength: Dp = 4.dp,
    gapLength: Dp = 3.dp,
): Modifier = drawWithCache {
    val strokeWidthPx = strokeWidth.toPx()
    val halfStroke = strokeWidthPx / 2f
    val stroke = Stroke(
        width = strokeWidthPx,
        pathEffect = PathEffect.dashPathEffect(
            intervals = floatArrayOf(dashLength.toPx(), gapLength.toPx()),
        ),
    )
    onDrawBehind {
        drawRoundRect(
            color = color,
            topLeft = Offset(halfStroke, halfStroke),
            size = Size(size.width - strokeWidthPx, size.height - strokeWidthPx),
            cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
            style = stroke,
        )
    }
}
