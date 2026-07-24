package com.iti.careerpilot.reports.presentation.screen.details.view.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.reports.R
import com.iti.careerpilot.reports.presentation.screen.components.performanceMetricLabel
import com.iti.careerpilot.reports.presentation.screen.details.uimodels.PerformanceMetricType
import com.iti.careerpilot.reports.presentation.screen.details.uimodels.PerformanceMetricUiModel
import com.iti.careerpilot.reports.presentation.screen.details.util.RADAR_ANIMATION_DURATION_MILLIS
import com.iti.careerpilot.reports.presentation.screen.details.util.RADAR_AXES
import com.iti.careerpilot.reports.presentation.screen.details.util.RADAR_GRID_LEVELS
import com.iti.careerpilot.reports.presentation.screen.details.util.drawRadarPolygon
import com.iti.careerpilot.reports.presentation.screen.details.util.mapInPlace
import com.iti.careerpilot.reports.presentation.screen.details.util.normalizeRadarScore
import com.iti.careerpilot.reports.presentation.screen.details.util.radarVertices
import com.iti.careerpilot.reports.presentation.screen.details.util.scoreFor
import kotlinx.collections.immutable.ImmutableList

@Composable
fun RadarChart(
    metrics: ImmutableList<PerformanceMetricUiModel>,
    modifier: Modifier = Modifier,
) {
    val clarity = metrics.scoreFor(PerformanceMetricType.CLARITY)
    val confidence = metrics.scoreFor(PerformanceMetricType.CONFIDENCE)
    val pacing = metrics.scoreFor(PerformanceMetricType.PACING)
    val fillerWords = metrics.scoreFor(PerformanceMetricType.FILLER_WORDS)
    val content = metrics.scoreFor(PerformanceMetricType.CONTENT)
    val description = stringResource(
        R.string.reports_radar_summary,
        clarity,
        confidence,
        pacing,
        fillerWords,
        content,
    )
    val layoutDirection = LocalLayoutDirection.current
    val normalizedScores = remember(metrics, layoutDirection) {
        if (layoutDirection == LayoutDirection.Ltr) {
            floatArrayOf(
                clarity.toFloat(),
                confidence.toFloat(),
                pacing.toFloat(),
                fillerWords.toFloat(),
                content.toFloat(),
            )
        } else {
            floatArrayOf(
                clarity.toFloat(),
                content.toFloat(),
                fillerWords.toFloat(),
                pacing.toFloat(),
                confidence.toFloat(),
            )
        }.mapInPlace(::normalizeRadarScore)
    }
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
    val dataColor = MaterialTheme.colorScheme.primary
    var animationTarget by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(metrics) {
        animationTarget = 1f
    }
    val animationProgress = animateFloatAsState(
        targetValue = animationTarget,
        animationSpec = tween(
            durationMillis = RADAR_ANIMATION_DURATION_MILLIS,
            easing = FastOutSlowInEasing,
        ),
        label = "radarChartProgress",
    )

    Box(
        modifier = modifier
            .size(Dimens.RadarChartContainerSize)
            .semantics { contentDescription = description },
    ) {
        Canvas(
            modifier = Modifier
                .size(Dimens.RadarChartSize)
                .align(Alignment.Center),
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f - Dimens.SpaceXS.toPx()
            repeat(RADAR_GRID_LEVELS) { level ->
                val levelRadius = radius * (level + 1) / RADAR_GRID_LEVELS
                drawRadarPolygon(
                    vertices = radarVertices(FloatArray(RADAR_AXES) { 1f }, center, levelRadius),
                    color = gridColor,
                    style = Stroke(Dimens.RadarGridStrokeWidth.toPx()),
                )
            }
            radarVertices(FloatArray(RADAR_AXES) { 1f }, center, radius).forEach { vertex ->
                drawLine(
                    color = gridColor,
                    start = center,
                    end = vertex,
                    strokeWidth = Dimens.RadarGridStrokeWidth.toPx(),
                )
            }
            val dataVertices = radarVertices(
                scores = normalizedScores,
                center = center,
                radius = radius,
                progress = animationProgress.value,
            )
            drawRadarPolygon(
                vertices = dataVertices,
                color = dataColor.copy(alpha = 0.14f),
            )
            drawRadarPolygon(
                vertices = dataVertices,
                color = dataColor,
                style = Stroke(Dimens.RadarDataStrokeWidth.toPx()),
            )
        }

        RadarLabel(PerformanceMetricType.CLARITY, Modifier.align(Alignment.TopCenter))
        RadarLabel(PerformanceMetricType.CONFIDENCE, Modifier.align(Alignment.CenterEnd))
        RadarLabel(PerformanceMetricType.PACING, Modifier.align(Alignment.BottomEnd))
        RadarLabel(PerformanceMetricType.FILLER_WORDS, Modifier.align(Alignment.BottomStart))
        RadarLabel(PerformanceMetricType.CONTENT, Modifier.align(Alignment.CenterStart))
    }
}