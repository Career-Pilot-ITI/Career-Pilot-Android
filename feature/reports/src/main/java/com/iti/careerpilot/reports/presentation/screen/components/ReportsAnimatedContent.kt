package com.iti.careerpilot.reports.presentation.screen.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

enum class ReportsContentPhase {
    LOADING,
    ERROR,
    EMPTY,
    CONTENT,
}

@Composable
fun ReportsAnimatedContent(
    phase: ReportsContentPhase,
    modifier: Modifier = Modifier,
    content: @Composable (ReportsContentPhase) -> Unit,
) {
    AnimatedContent(
        targetState = phase,
        modifier = modifier,
        transitionSpec = { reportsFadeScaleTransition() },
        contentKey = { it },
        label = "reportsContentPhase",
    ) { targetPhase ->
        content(targetPhase)
    }
}

private fun reportsFadeScaleTransition(): ContentTransform =
    fadeIn(
        animationSpec = tween(
            durationMillis = ENTER_DURATION_MILLIS,
            easing = FastOutSlowInEasing,
        ),
    ) + scaleIn(
        initialScale = ENTER_INITIAL_SCALE,
        animationSpec = tween(
            durationMillis = ENTER_DURATION_MILLIS,
            easing = FastOutSlowInEasing,
        ),
    ) togetherWith fadeOut(
        animationSpec = tween(durationMillis = EXIT_DURATION_MILLIS),
    ) + scaleOut(
        targetScale = EXIT_TARGET_SCALE,
        animationSpec = tween(durationMillis = EXIT_DURATION_MILLIS),
    )

private const val ENTER_DURATION_MILLIS = 350
private const val EXIT_DURATION_MILLIS = 180
private const val ENTER_INITIAL_SCALE = 0.96f
private const val EXIT_TARGET_SCALE = 0.98f
