package com.iti.careerpilot.practicesession.presentation.practicescreen.screen.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
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

private val FabSize = 112.dp
private val FabGlowSize = 192.dp
private val FabGradientColors = listOf(
    Color(0xFF6D5DF6), // indigo
    Color(0xFF9D5CF9), // violet
    Color(0xFF3ED6C6), // teal
)

@Composable
fun AiTalkingAnimation(
    isPulsing: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ai_fab")

    // Continuous rotation of the outer ring
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing)
        ),
        label = "ring_rotation"
    )

    // Breathing effect for the glow alpha
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = if (isPulsing) 0.4f else 0.25f,
        targetValue = if (isPulsing) 0.8f else 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isPulsing) 1000 else 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    // Dynamic scale for the glow/shadow based on isPulsing
    val glowScale by infiniteTransition.animateFloat(
        initialValue = if (isPulsing) 1.0f else 0.95f,
        targetValue = if (isPulsing) 1.35f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isPulsing) 1000 else 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_scale"
    )

    // Slight pulsing of the button itself when active
    val buttonScale by animateFloatAsState(
        targetValue = if (isPulsing) 1.05f else 1f,
        animationSpec = tween(500),
        label = "button_scale"
    )

    Box(
        modifier = modifier
            .size(FabSize)
            .graphicsLayer {
                scaleX = buttonScale
                scaleY = buttonScale
            },
        contentAlignment = Alignment.Center,
    ) {
        // Shadow/Glow Layer
        Box(
            modifier = Modifier
                .requiredSize(FabGlowSize)
                .graphicsLayer {
                    alpha = glowAlpha
                    scaleX = glowScale
                    scaleY = glowScale
                }
                .blur(24.dp)
                .background(
                    brush = Brush.radialGradient(
                        0.5f to FabGradientColors[1].copy(alpha = 0.8f),
                        0.8f to Color.Transparent
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
                        brush = Brush.sweepGradient(
                            colors = FabGradientColors + FabGradientColors.first()
                        ),
                        shape = CircleShape
                    )
            )
        }

        // AI Lottie Animation overlay
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
