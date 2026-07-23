package com.iti.careerpilot.practicesession.presentation.screen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .clickable(enabled = enabled, onClickLabel = contentDescription, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        GradientIcon(icon = icon, modifier = Modifier.size(size * 0.5f))
    }
}
