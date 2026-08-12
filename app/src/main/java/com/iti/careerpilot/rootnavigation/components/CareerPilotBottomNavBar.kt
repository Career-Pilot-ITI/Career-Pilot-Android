package com.iti.careerpilot.rootnavigation.components

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.exyte.animatednavbar.AnimatedNavigationBar
import com.exyte.animatednavbar.animation.balltrajectory.Parabolic
import com.exyte.animatednavbar.animation.indendshape.Height
import com.exyte.animatednavbar.animation.indendshape.shapeCornerRadius

private const val BALL_ANIMATION_DURATION = 500
private const val INDENT_ANIMATION_DURATION = 1_000


@Composable
fun CareerPilotBottomNavBar(
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedNavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .selectableGroup(),
        selectedIndex = selectedIndex,

        ballColor = MaterialTheme.colorScheme.primary,
        barColor = MaterialTheme.colorScheme.surface,

        cornerRadius = shapeCornerRadius(28.dp),

        ballAnimation = Parabolic(
            animationSpec = tween(
                durationMillis = BALL_ANIMATION_DURATION,
                easing = LinearOutSlowInEasing,
            ),
        ),

        indentAnimation = Height(
            indentWidth = 56.dp,
            indentHeight = 15.dp,
            animationSpec = tween(
                durationMillis = INDENT_ANIMATION_DURATION,
                easing = { progress ->
                    OvershootInterpolator()
                        .getInterpolation(progress)
                },
            ),
        ),
    ) {
        content()
    }
}

