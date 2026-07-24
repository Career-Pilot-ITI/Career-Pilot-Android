package com.iti.careerpilot.reports.presentation.screen.details.util

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import com.iti.careerpilot.reports.presentation.screen.details.uimodels.PerformanceMetricType
import com.iti.careerpilot.reports.presentation.screen.details.uimodels.PerformanceMetricUiModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


fun FloatArray.mapInPlace(transform: (Float) -> Float): FloatArray = apply {
    indices.forEach { index -> this[index] = transform(this[index]) }
}

fun List<PerformanceMetricUiModel>.scoreFor(type: PerformanceMetricType): Int =
    firstOrNull { it.type == type }?.score ?: 0

fun DrawScope.drawRadarPolygon(
    vertices: Array<Offset>,
    color: Color,
    style: DrawStyle = Fill,
) {
    if (vertices.isEmpty()) return
    val path = Path().apply {
        moveTo(vertices.first().x, vertices.first().y)
        for (index in 1 until vertices.size) {
            lineTo(vertices[index].x, vertices[index].y)
        }
        close()
    }
    drawPath(path = path, color = color, style = style)
}

const val RADAR_AXES = 5
const val RADAR_GRID_LEVELS = 4
const val RADAR_ANIMATION_DURATION_MILLIS = 1_000


internal fun normalizeRadarScore(score: Float): Float = (score / 100f).coerceIn(0f, 1f)

internal fun radarVertices(
    scores: FloatArray,
    center: Offset,
    radius: Float,
    progress: Float = 1f,
): Array<Offset> {
    val boundedProgress = progress.coerceIn(0f, 1f)
    return Array(scores.size) { index ->
        val angle = -PI / 2 + 2 * PI * index / scores.size
        val animatedScore = scores[index] * boundedProgress
        Offset(
            x = center.x + cos(angle).toFloat() * radius * animatedScore,
            y = center.y + sin(angle).toFloat() * radius * animatedScore,
        )
    }
}