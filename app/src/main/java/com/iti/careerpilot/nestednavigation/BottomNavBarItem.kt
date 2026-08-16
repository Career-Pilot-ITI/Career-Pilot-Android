package com.iti.careerpilot.nestednavigation

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.visible
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

private const val ICON_BOUNCE_DURATION = 130

@Composable
fun BottomNavBarItem(
    onClick: () -> Unit,
    isSelected: Boolean,
    @DrawableRes icon: Int,
    label: String,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val interactionSource = remember { MutableInteractionSource() }

    val bounceOffset = remember { Animatable(0f) }
    val iconScale = remember { Animatable(1f) }

    val iconColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.50f)
        },
        animationSpec = tween(durationMillis = 200),
        label = "bottomNavIconColor",
    )

    val labelColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.50f)
        },
        animationSpec = tween(durationMillis = 200),
        label = "bottomNavLabelColor",
    )

    LaunchedEffect(isSelected) {
        if (!isSelected) {
            bounceOffset.snapTo(0f)
            iconScale.snapTo(1f)
            return@LaunchedEffect
        }

        coroutineScope {
            launch {
                bounceOffset.snapTo(0f)

                bounceOffset.animateTo(
                    targetValue = -10f,
                    animationSpec = tween(
                        durationMillis = ICON_BOUNCE_DURATION,
                        easing = FastOutLinearInEasing,
                    ),
                )

                bounceOffset.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = 0.38f,
                        stiffness = Spring.StiffnessLow,
                    ),
                )
            }

            launch {
                iconScale.snapTo(1f)

                iconScale.animateTo(
                    targetValue = 1.18f,
                    animationSpec = tween(
                        durationMillis = ICON_BOUNCE_DURATION,
                    ),
                )

                iconScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = 0.42f,
                        stiffness = Spring.StiffnessLow,
                    ),
                )
            }
        }
    }

    Box(
        modifier = modifier.selectable(
            selected = isSelected,
            onClick = onClick,
            role = Role.Tab,
            interactionSource = interactionSource,
            indication = null,
        ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Spacer(Modifier.height(2.dp))
            Icon(
                painter = painterResource(id = icon),
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer {
                        translationY = with(density) {
                            bounceOffset.value.dp.toPx()
                        }

                        scaleX = iconScale.value
                        scaleY = iconScale.value
                    },
            )

            Spacer(Modifier.height(2.dp))

            AnimatedVisibility(isSelected) {
                Text(
                    modifier = Modifier.visible(isSelected),
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = labelColor
                    )
                )
            }
        }
    }
}