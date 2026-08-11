package com.iti.careerpilot.practicesession.presentation.resultscreen.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.practicesession.R
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun BodyLanguageRadarChart(
    eyeContactScore: Int,
    postureScore: Int,
    handGesturesScore: Int,
    facialExpressionScore: Int,
    modifier: Modifier = Modifier,
    labels: List<String> = listOf(
        stringResource(R.string.radar_eye_contact),
        stringResource(R.string.radar_posture),
        stringResource(R.string.radar_hand_stability),
        stringResource(R.string.radar_facial_expressions),
    ),
) {
    val animEye by animateFloatAsState(
        targetValue = eyeContactScore.coerceIn(0, 100) / 100f,
        animationSpec = tween(1000),
        label = "animEye"
    )
    val animPosture by animateFloatAsState(
        targetValue = postureScore.coerceIn(0, 100) / 100f,
        animationSpec = tween(1000),
        label = "animPosture"
    )
    val animHand by animateFloatAsState(
        targetValue = handGesturesScore.coerceIn(0, 100) / 100f,
        animationSpec = tween(1000),
        label = "animHand"
    )
    val animFacial by animateFloatAsState(
        targetValue = facialExpressionScore.coerceIn(0, 100) / 100f,
        animationSpec = tween(1000),
        label = "animFacial"
    )

    val scores = listOf(animEye, animPosture, animHand, animFacial)
    val angles = listOf(-Math.PI / 2, 0.0, Math.PI / 2, Math.PI)

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val outlineColor = MaterialTheme.colorScheme.outline
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textStyle = MaterialTheme.typography.labelSmall.copy(
        color = textColor,
        fontWeight = FontWeight.SemiBold
    )

    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val maxRadius = (minOf(centerX, centerY) - 44.dp.toPx()).coerceAtLeast(10f)

            // 1. Draw multi-layer polygon web (25%, 50%, 75%, 100%)
            val gridFractions = listOf(0.25f, 0.50f, 0.75f, 1.0f)
            gridFractions.forEach { fraction ->
                val webPath = Path()
                angles.forEachIndexed { index, angle ->
                    val r = maxRadius * fraction
                    val x = centerX + r * cos(angle).toFloat()
                    val y = centerY + r * sin(angle).toFloat()
                    if (index == 0) webPath.moveTo(x, y) else webPath.lineTo(x, y)
                }
                webPath.close()
                drawPath(
                    path = webPath,
                    color = outlineColor.copy(alpha = 0.2f),
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // 2. Draw 4 radial axis spokes
            angles.forEach { angle ->
                val endX = centerX + maxRadius * cos(angle).toFloat()
                val endY = centerY + maxRadius * sin(angle).toFloat()
                drawLine(
                    color = outlineColor.copy(alpha = 0.2f),
                    start = Offset(centerX, centerY),
                    end = Offset(endX, endY),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // 3. Construct Data Polygon
            val dataPoints = angles.mapIndexed { index, angle ->
                val r = maxRadius * scores[index].coerceIn(0f, 1f)
                val x = centerX + r * cos(angle).toFloat()
                val y = centerY + r * sin(angle).toFloat()
                Offset(x, y)
            }

            val dataPath = Path().apply {
                dataPoints.forEachIndexed { index, point ->
                    if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
                }
                close()
            }

            // Fill data polygon
            drawPath(
                path = dataPath,
                color = primaryColor.copy(alpha = 0.25f),
                style = Fill
            )

            // Stroke data polygon border
            drawPath(
                path = dataPath,
                color = primaryColor,
                style = Stroke(width = 2.5.dp.toPx())
            )

            // 4. Vertex circle dots at each score coordinate
            dataPoints.forEach { point ->
                drawCircle(
                    color = primaryColor,
                    radius = 4.dp.toPx(),
                    center = point
                )
                drawCircle(
                    color = surfaceColor,
                    radius = 2.dp.toPx(),
                    center = point
                )
            }

            // 5. Draw labels outside the radar chart web
            labels.forEachIndexed { index, label ->
                if (index < angles.size) {
                    val angle = angles[index]
                    val labelRadius = maxRadius + 14.dp.toPx()
                    val lx = centerX + labelRadius * cos(angle).toFloat()
                    val ly = centerY + labelRadius * sin(angle).toFloat()

                    val layoutResult = textMeasurer.measure(label, textStyle)
                    val w = layoutResult.size.width.toFloat()
                    val h = layoutResult.size.height.toFloat()

                    val topLeft = when (index) {
                        0 -> Offset(lx - w / 2f, ly - h) // Top (Eye Contact)
                        1 -> Offset(lx + 4.dp.toPx(), ly - h / 2f) // Right (Posture)
                        2 -> Offset(lx - w / 2f, ly) // Bottom (Hand Stability)
                        3 -> Offset(lx - w - 4.dp.toPx(), ly - h / 2f) // Left (Facial Expressions)
                        else -> Offset(lx, ly)
                    }

                    drawText(
                        textMeasurer = textMeasurer,
                        text = label,
                        topLeft = topLeft,
                        style = textStyle
                    )
                }
            }
        }
    }
}
