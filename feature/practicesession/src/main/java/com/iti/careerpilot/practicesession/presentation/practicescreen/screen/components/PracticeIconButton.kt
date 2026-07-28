package com.iti.careerpilot.practicesession.presentation.practicescreen.screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.core.designsystem.CareerPilotTheme

@Composable
fun PracticeIconButton(
    iconId: Int,
    descriptionId: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val extendedColors = CareerPilotTheme.extendedColors

    val backgroundBrush = remember(extendedColors) {
        Brush.linearGradient(
            colors = listOf(
                extendedColors.radialGradientStart,
                extendedColors.radialGradientEnd,
            ),
            start = Offset(0f, 0f),
        )
    }

    Box(
        modifier = modifier
            .size(52.dp)
            .shadow(
                elevation = 6.dp,
                shape = CircleShape,
                ambientColor = extendedColors.radialGradientStart,
                spotColor = extendedColors.radialGradientStart,
            )
            .clip(CircleShape)
            .background(backgroundBrush)
            .border(1.dp, extendedColors.actionBorder.copy(alpha = 0.4f), CircleShape)
            .clickable(
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(iconId),
            contentDescription = stringResource(descriptionId),
            tint = MaterialTheme.colorScheme.onBackground,
        )
    }
}
