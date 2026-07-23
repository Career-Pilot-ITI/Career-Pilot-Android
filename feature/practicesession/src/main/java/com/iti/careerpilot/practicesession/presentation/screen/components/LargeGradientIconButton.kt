package com.iti.careerpilot.practicesession.presentation.screen.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.core.designsystem.common.GradientIcon

@Composable
fun LargeGradientIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    contentDescription: String? = null,
    enabled: Boolean = true,
    size: Dp = 72.dp,
    isOutlined: Boolean = false
) {
    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.secondary
        )
    )

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .then(
                if (isOutlined) Modifier.background(Color.Transparent)
                else Modifier.background(gradientBrush)
            )
            .clickable(enabled = enabled, onClickLabel = contentDescription, onClick = onClick)
            .then(
                if (isOutlined) Modifier
                    .background(Color.Transparent)
                    .padding(2.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isOutlined) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = gradientBrush,
                    radius = (size.toPx() / 2) - 1.dp.toPx(),
                    style = Stroke(width = 3.dp.toPx())
                )
            }
            GradientIcon(icon = icon, modifier = Modifier.size(size * 0.5f))
        } else {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(size * 0.5f)
            )
        }
    }
}
