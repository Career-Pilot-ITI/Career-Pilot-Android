package com.iti.careerpilot.practicesession.presentation.practicescreen.screen.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
import com.iti.careerpilot.practicesession.R

@Composable
fun QuestionCard(
    questionOrder: Int?,
    maxQuestions: Int?,
    questionText: String?,
    isReadingQuestion: Boolean,
    onPlayClick: () -> Unit,
    onStopClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val scrollbarColor = MaterialTheme.colorScheme.primary

    CareerPilotCard(
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 8.dp, bottom = 8.dp)
                .clip(RoundedCornerShape(bottomStart = 8.dp)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val questionLabel = if (questionOrder != null && maxQuestions != null) {
                    stringResource(R.string.question_x_of_y, questionOrder, maxQuestions)
                } else {
                    stringResource(R.string.question_number, questionOrder ?: "")
                }
                Text(
                    text = questionLabel,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                IconButton(
                    onClick = if (isReadingQuestion) onStopClick else onPlayClick,
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(
                            if (isReadingQuestion) R.drawable.ic_stop else R.drawable.ic_play
                        ),
                        contentDescription = stringResource(
                            if (isReadingQuestion) R.string.stop else R.string.play
                        ),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 120.dp)
                    .verticalScrollbar(scrollState = scrollState, color = scrollbarColor)
                    .verticalScroll(scrollState)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = questionText ?: stringResource(R.string.loading_question),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Start
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Standard, smooth vertical scrollbar drawn directly in the Draw phase.
 * Defers state reads to drawing so scrolling never causes recompositions.
 */
private fun Modifier.verticalScrollbar(
    scrollState: ScrollState,
    color: Color,
    width: Dp = 4.dp,
    minThumbHeight: Dp = 20.dp,
    paddingEnd: Dp = 0.dp,
): Modifier = drawWithContent {
    drawContent()

    val maxValue = scrollState.maxValue
    if (maxValue > 0) {
        val visibleHeight = size.height
        val totalHeight = visibleHeight + maxValue
        val minThumbPx = minThumbHeight.toPx()
        val thumbHeight = (visibleHeight * (visibleHeight / totalHeight))
            .coerceIn(minThumbPx, visibleHeight)
        val scrollRatio = scrollState.value.toFloat() / maxValue.toFloat()
        val thumbOffsetY = scrollRatio * (visibleHeight - thumbHeight)

        val widthPx = width.toPx()
        val paddingEndPx = paddingEnd.toPx()
        val thumbOffsetX = size.width - widthPx - paddingEndPx
        val cornerRadius = CornerRadius(widthPx / 2f, widthPx / 2f)

        // Draw subtle background track
        drawRoundRect(
            color = color.copy(alpha = 0.15f),
            topLeft = Offset(thumbOffsetX, 0f),
            size = Size(widthPx, visibleHeight),
            cornerRadius = cornerRadius
        )

        // Draw scroll thumb
        drawRoundRect(
            color = color,
            topLeft = Offset(thumbOffsetX, thumbOffsetY),
            size = Size(widthPx, thumbHeight),
            cornerRadius = cornerRadius
        )
    }
}
