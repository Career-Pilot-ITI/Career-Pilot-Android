package com.iti.careerpilot.features.paywall.presentation.view.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.iti.careerpilot.core.designsystem.CareerPilotPalette
import com.iti.careerpilot.core.designsystem.CareerPilotPalette as CareerPilotColors
import com.iti.careerpilot.core.designsystem.CareerPilotShapes
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.common.GradientIcon
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
import com.iti.careerpilot.payment.R
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SuccessAnimation(
    modifier: Modifier = Modifier
) {
    var startAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        startAnimation = true
    }
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary

    val transition = updateTransition(targetState = startAnimation, label = "success")

    val scale by transition.animateFloat(
        transitionSpec = { spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow) },
        label = "scale"
    ) { if (it) 1f else 0f }

    val ringScale by transition.animateFloat(
        transitionSpec = { tween(1200, easing = FastOutSlowInEasing) },
        label = "ringScale"
    ) { if (it) 1.5f else 0.5f }

    val ringAlpha by transition.animateFloat(
        transitionSpec = { tween(1200, easing = LinearEasing) },
        label = "ringAlpha"
    ) { if (it) 0f else 1f }

    val particlesProgress by transition.animateFloat(
        transitionSpec = { tween(1600, easing = FastOutSlowInEasing) },
        label = "particles"
    ) { if (it) 1f else 0f }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = modifier.size(Dimens.AnimationBoxSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val maxRadius = size.width / 2

            val particleCount = 12
            for (i in 0 until particleCount) {
                val angle = (i * (360f / particleCount)) * (Math.PI / 180f)
                val distance = maxRadius * particlesProgress

                val x = center.x + (cos(angle) * distance).toFloat()
                val y = center.y + (sin(angle) * distance).toFloat()

                val color = if (i % 2 == 0) CareerPilotColors.green else CareerPilotPalette.amber
                val particleAlpha = 1f - particlesProgress

                if (particleAlpha > 0) {
                    drawCircle(
                        color = color.copy(alpha = particleAlpha),
                        radius = Dimens.SpaceS.toPx() * (1f - particlesProgress * 0.5f),
                        center = Offset(x, y)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .size(Dimens.ErrorRingSize)
                .graphicsLayer {
                    scaleX = ringScale
                    scaleY = ringScale
                    alpha = ringAlpha
                }
                .border(
                    Dimens.SpaceXS,
                    Brush.linearGradient(listOf(primary, secondary)),
                    CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(Dimens.ErrorRingSize)
                .graphicsLayer {
                    val currentPulse = if (startAnimation) pulseScale else 1f
                    scaleX = scale * currentPulse
                    scaleY = scale * currentPulse
                }
                .background(CareerPilotColors.green.copy(alpha = 0.2f), CircleShape)
                .border(Dimens.BorderMedium, CareerPilotColors.green, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = CareerPilotColors.green,
                modifier = Modifier.size(Dimens.ErrorIconSize)
            )
        }
    }
}

@Composable
fun SuccessBenefitsCard(
    benefits: List<String>,
    modifier: Modifier = Modifier
) {
    CareerPilotCard(
        elevation = Dimens.SpaceS,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpaceL),
            horizontalAlignment = Alignment.Start
        ) {
            benefits.forEach { benefitText ->
                key(benefitText) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Dimens.SpaceXS)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = CareerPilotColors.green,
                            modifier = Modifier.size(Dimens.SpaceL)
                        )
                        Spacer(modifier = Modifier.width(Dimens.SpaceS))
                        Text(
                            text = benefitText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CoinPurchaseSuccessCard(
    coinCount: Int,
    totalBalance: Int,
    modifier: Modifier = Modifier
) {
    CareerPilotCard(
        elevation = Dimens.SpaceS,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpaceL)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(Dimens.SpaceM)
                    )
                    .padding(horizontal = Dimens.SpaceM, vertical = Dimens.SpaceM),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GradientIcon(
                        icon = Icons.Rounded.Star,
                        modifier = Modifier.size(Dimens.IconSizeMedium),
                        leftColor = CareerPilotPalette.yellow,
                        middleColor = CareerPilotPalette.amberLight,
                        rightColor = CareerPilotPalette.amber
                    )
                    Spacer(modifier = Modifier.width(Dimens.SpaceS))
                    Text(
                        text = stringResource(R.string.paywall_coins_successful_wallet_balance),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = stringResource(R.string.paywall_coin_pack, totalBalance),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CareerPilotPalette.amber
                )
            }

            Spacer(modifier = Modifier.height(Dimens.SpaceM))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = CareerPilotColors.green,
                    modifier = Modifier
                        .padding(top = Dimens.SpaceXXXS)
                        .size(Dimens.SpaceL)
                )
                Spacer(modifier = Modifier.width(Dimens.SpaceS))
                Text(
                    text = stringResource(R.string.paywall_coins_successful_info),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun FailedAnimation(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shake")
    val errorColor = MaterialTheme.colorScheme.error

    val shakeOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1000
                0f at 0
                -20f at 100
                20f at 200
                -10f at 300
                10f at 400
                0f at 500
                0f at 1000
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "shake_offset"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "error_scale"
    )

    Box(
        modifier = modifier
            .size(Dimens.ErrorRingSize)
            .graphicsLayer {
                translationX = shakeOffset
                scaleX = scale
                scaleY = scale
            }
            .background(errorColor.copy(alpha = 0.1f), CircleShape)
            .border(Dimens.BorderThick, errorColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = null,
            tint = errorColor,
            modifier = Modifier.size(Dimens.ErrorIconSize)
        )
    }
}

@Composable
fun PaymentErrorCard(
    errorMessage: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    CareerPilotCard(
        elevation = Dimens.SpaceS,
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = Dimens.BorderThin,
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                shape = CareerPilotShapes.medium
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpaceM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(Dimens.SpaceL)
            )
            Spacer(modifier = Modifier.width(Dimens.SpaceS))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
fun FloatingScannerCardAnimation(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "processing")

    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    val scanPosition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scan"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = modifier.size(Dimens.AnimationBoxSize),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(Dimens.ErrorRingSize)
                .graphicsLayer {
                    scaleX = pulseScale
                    scaleY = pulseScale
                    alpha = pulseAlpha
                }
                .border(Dimens.BorderMedium, CareerPilotPalette.amber, CircleShape)
        )

        Box(
            modifier = Modifier
                .size(Dimens.CardPulseSize)
                .background(CareerPilotPalette.amber.copy(alpha = 0.1f), CircleShape)
                .border(Dimens.BorderThin, CareerPilotPalette.amber.copy(alpha = 0.3f), CircleShape)
        )

        Box(
            modifier = Modifier
                .graphicsLayer { translationY = floatOffset }
                .size(width = Dimens.ErrorRingSize, height = Dimens.LockIconBoxSize)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(Dimens.SpaceM)
                )
                .border(
                    width = Dimens.BorderThin,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(Dimens.SpaceM)
                )
                .clip(RoundedCornerShape(Dimens.SpaceM))
        ) {
            Box(
                modifier = Modifier
                    .padding(Dimens.SpaceM)
                    .size(width = Dimens.SpaceXL, height = Dimens.SpaceM)
                    .background(CareerPilotPalette.amber.copy(alpha = 0.5f), RoundedCornerShape(Dimens.SpaceXS))
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(Dimens.SpaceM)
                    .size(width = Dimens.SpaceXXXXL, height = Dimens.SpaceXS)
                    .background(MaterialTheme.colorScheme.outline, RoundedCornerShape(Dimens.SpaceXS))
            )

            Icon(
                imageVector = Icons.Rounded.CreditCard,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(Dimens.SpaceM)
                    .size(Dimens.SpaceXL)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.ScanBarHeight)
                    .graphicsLayer {
                        translationY = (scanPosition * (Dimens.LockIconBoxSize.toPx() + Dimens.ScanBarHeight.toPx())) - Dimens.ScanBarHeight.toPx()
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    CareerPilotPalette.amber.copy(alpha = 0.4f)
                                )
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(Dimens.BorderMedium)
                        .background(CareerPilotPalette.amber)
                )
            }
        }
    }
}

@Composable
fun LoadingDots(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until 3) {
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(400, delayMillis = i * 200, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot_alpha_$i"
            )
            Box(
                modifier = Modifier
                    .size(Dimens.SpaceS)
                    .graphicsLayer { this.alpha = alpha }
                    .background(CareerPilotPalette.amber, CircleShape)
            )
        }
    }
}
