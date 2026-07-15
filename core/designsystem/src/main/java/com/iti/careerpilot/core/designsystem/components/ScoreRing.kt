package com.iti.careerpilot.core.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.core.designsystem.CareerPilotPalette
import com.iti.careerpilot.core.designsystem.CareerPilotTheme
import com.iti.careerpilot.core.designsystem.CareerPilotTypography

@Composable
fun ScoreRing(
    progress: Float,
    progressColor: Color,
    trackColor: Color,
    modifier: Modifier = Modifier,
    strokeWidthDp: Dp = 8.dp,
    animationSpec: AnimationSpec<Float> = tween(
        durationMillis = 1000, 
        easing = FastOutSlowInEasing
    ),
    centerContent: @Composable (animatedProgress: Float) -> Unit = { animatedProgress ->
        Text(
            text = "${(animatedProgress * 100).toInt()}%",
            style = CareerPilotTypography.titleLarge
        )
    }
) {
    var animationTarget by remember { mutableStateOf(0f) }
    
    LaunchedEffect(progress) {
        animationTarget = progress.coerceIn(0f, 1f)
    }

    val animatedProgress by animateFloatAsState(
        targetValue = animationTarget,
        animationSpec = animationSpec,
        label = "progressAnimation"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val strokeWidth = strokeWidthDp.toPx()
            val startAngle = -90f
            val sweepAngle = animatedProgress * 360f
            
            val diameter = size.minDimension - strokeWidth
            val arcSize = Size(diameter, diameter)
            val topLeftOffset = Offset(
                (size.width - diameter) / 2f,
                (size.height - diameter) / 2f
            )

            // Draw background track
            drawCircle(
                color = trackColor,
                radius = diameter / 2f,
                style = Stroke(width = strokeWidth)
            )

            // Draw progress arc
            if (sweepAngle > 0f) {
                drawArc(
                    color = progressColor,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeftOffset,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }
        centerContent(animatedProgress)
    }
}

@Preview(showBackground = true)
@Composable
fun ScoreRingPreview() {
    CareerPilotTheme {
        ScoreRing(
            progress = 0.85f,
            progressColor = CareerPilotPalette.green,
            trackColor = CareerPilotPalette.gray200,
            modifier = Modifier.size(100.dp).padding(16.dp)
        )
    }
}
