package com.iti.careerpilot.core.designsystem.components

import android.content.res.Configuration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.iti.careerpilot.core.designsystem.CareerPilotPalette
import com.iti.careerpilot.core.designsystem.CareerPilotTheme
import com.iti.careerpilot.core.designsystem.Dimens

@Composable
fun SuccessCheckmark(
    modifier: Modifier = Modifier,
    size: Dp = Dimens.SuccessRingSize,
) {
    val popScale = remember { Animatable(0.6f) }
    val checkProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        popScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
        )
    }
    LaunchedEffect(Unit) {
        checkProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 520,
                delayMillis = 160,
                easing = FastOutSlowInEasing
            ),
        )
    }

    Canvas(
        modifier = modifier
            .size(size)
            .scale(popScale.value),
    ) {
        val dim = this.size.minDimension
        val center = Offset(dim / 2f, dim / 2f)

        drawCircle(
            color = CareerPilotPalette.green.copy(alpha = 0.18f),
            radius = dim / 2f,
        )

        drawCircle(
            color = CareerPilotPalette.green,
            radius = dim * 0.36f,
        )

        val checkPath = Path().apply {
            moveTo(dim * 0.36f, dim * 0.51f)
            lineTo(dim * 0.46f, dim * 0.62f)
            lineTo(dim * 0.65f, dim * 0.40f)
        }
        val measure = PathMeasure().apply { setPath(checkPath, false) }
        val revealed = Path()
        measure.getSegment(0f, measure.length * checkProgress.value, revealed, true)
        drawPath(
            path = revealed,
            color = CareerPilotPalette.white,
            style = Stroke(width = dim * 0.055f, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}

@Preview(
    name = "Light",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Dark",
    showBackground = true,
    backgroundColor = 0xFF121212,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun SuccessCheckmarkPreview() {
    CareerPilotTheme {
        SuccessCheckmark()
    }
}