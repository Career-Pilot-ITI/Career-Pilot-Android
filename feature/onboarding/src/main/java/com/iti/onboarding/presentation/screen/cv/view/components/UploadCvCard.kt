package com.iti.onboarding.presentation.screen.cv.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.iti.onboarding.R
import com.iti.onboarding.presentation.screen.cv.state.CvUploadStage
import com.iti.onboarding.presentation.screen.cv.state.SelectedCvUiModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UploadCvCard(
    selectedFile: SelectedCvUiModel?,
    stage: CvUploadStage,
    uploadProgress: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val isUploaded = stage == CvUploadStage.UPLOADED
    val borderColor = if (isUploaded) {
        colors.primary
    } else {
        colors.outline
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(190.dp)
            .background(
                color = colors.surface,
                shape = MaterialTheme.shapes.large,
            )
            .clip(RoundedCornerShape(20.dp))
            .dashedRoundedBorder(
                color = borderColor,
                cornerRadius = 20.dp,
            )
            .clickable(
                enabled = stage != CvUploadStage.PREPARING &&
                    stage != CvUploadStage.UPLOADING,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        when (stage) {
            CvUploadStage.EMPTY -> EmptyCvContent()

            CvUploadStage.PREPARING -> PreparingCvContent()

            CvUploadStage.SELECTED -> SelectedCvContent(
                selectedFile = selectedFile,
                statusText = stringResource(R.string.cv_ready_to_upload),
            )

            CvUploadStage.UPLOADING -> UploadingCvContent(
                selectedFile = selectedFile,
                progress = uploadProgress,
            )

            CvUploadStage.UPLOADED -> UploadedCvContent(
                selectedFile = selectedFile,
            )
        }
    }
}


private fun Modifier.dashedRoundedBorder(
    color: androidx.compose.ui.graphics.Color,
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
            intervals = floatArrayOf(
                dashLength.toPx(),
                gapLength.toPx(),
            ),
        ),
    )

    onDrawBehind {
        drawRoundRect(
            color = color,
            topLeft = Offset(halfStroke, halfStroke),
            size = Size(
                width = size.width - strokeWidthPx,
                height = size.height - strokeWidthPx,
            ),
            cornerRadius = CornerRadius(
                x = cornerRadius.toPx(),
                y = cornerRadius.toPx(),
            ),
            style = stroke,
        )
    }
}
