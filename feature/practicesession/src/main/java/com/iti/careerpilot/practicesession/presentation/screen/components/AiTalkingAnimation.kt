package com.iti.careerpilot.practicesession.presentation.screen.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.iti.careerpilot.practicesession.R

private val FabSize = 80.dp
private val FabGlowSize = 120.dp
private val FabGradientColors = listOf(
    Color(0xFF6D5DF6), // indigo
    Color(0xFF9D5CF9), // violet
    Color(0xFF3ED6C6), // teal
)

@Composable
fun AiTalkingAnimation(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPulsing: Boolean = false,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ai_fab")
    
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing)
        ),
        label = "ring_rotation"
    )

    val targetAlpha = if (isPulsing) 0.85f else 0.65f
    val duration = if (isPulsing) 1000 else 1800

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = targetAlpha,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )
    Box(
        modifier = modifier
            .size(FabSize),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .requiredSize(FabGlowSize)
                .graphicsLayer { alpha = glowAlpha }
                .blur(18.dp)
                .background(
                    brush = Brush.radialGradient(
                        0.6f to FabGradientColors[1],
                        0.7f to Color.Transparent
                    ),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(FabSize)
                .clickable(
                    onClick = onClick,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationZ = ringRotation }
                    .background(
                        brush = Brush.sweepGradient(FabGradientColors + FabGradientColors.first()),
                        shape = CircleShape
                    )
            )
        }
        val composition by rememberLottieComposition(
            LottieCompositionSpec.RawRes(R.raw.lottie_ai_button)
        )
        val progress by animateLottieCompositionAsState(
            composition = composition,
            iterations = LottieConstants.IterateForever
        )
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.requiredSize(FabGlowSize)
        )
    }
}