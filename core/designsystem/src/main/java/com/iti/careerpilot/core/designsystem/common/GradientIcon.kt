package com.iti.careerpilot.core.designsystem.common

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun GradientIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    leftColor: Color = MaterialTheme.colorScheme.primary,
    middleColor: Color = MaterialTheme.colorScheme.tertiary,
    rightColor: Color = MaterialTheme.colorScheme.secondary,
) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.inverseSurface,
        modifier = modifier
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithContent {
                drawContent()
                drawRect(
                    brush = Brush.linearGradient(
                        0f to leftColor,
                        0.5f to middleColor,
                        1f to rightColor,
                    ),
                    blendMode = BlendMode.SrcAtop
                )
            }
    )
}