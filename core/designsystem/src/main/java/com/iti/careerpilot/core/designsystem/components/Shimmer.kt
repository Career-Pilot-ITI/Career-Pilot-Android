package com.iti.careerpilot.core.designsystem.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import com.iti.careerpilot.core.designsystem.Dimens

/**
 * Modifier extension that applies a shimmer loading animation effect to a composable.
 *
 * @param isLoading Whether the shimmer animation should be active. If false, returns the modifier unmodified.
 * @param shape The shape of the background to apply the shimmer to. Defaults to RoundedCornerShape(Dimens.SpaceM).
 */
fun Modifier.shimmerLoading(
    isLoading: Boolean = true,
    shape: Shape = RoundedCornerShape(Dimens.SpaceM)
): Modifier = composed {
    if (!isLoading) return@composed Modifier

    val shimmerColors = listOf(
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    )
    val transition = rememberInfiniteTransition(label = "shimmerLoadingTransition")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerLoadingTranslate"
    )

    this.drawWithContent {
        drawContent()
        val brush = Brush.linearGradient(
            colors = shimmerColors,
            start = Offset.Zero,
            end = Offset(x = translateAnim, y = translateAnim)
        )
        drawOutline(
            outline = shape.createOutline(size, layoutDirection, this),
            brush = brush
        )
    }
}

