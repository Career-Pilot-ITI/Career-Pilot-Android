package com.iti.careerpilot.core.designsystem

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp

fun Modifier.softShadow(
    elevation: Dp = 16.dp,
    shape: Shape = CareerPilotShapes.medium,
) = this.then(
    Modifier.shadow(
        shape = shape,
        elevation = elevation,
        spotColor = CareerPilotPalette.navy,
        ambientColor = CareerPilotPalette.navy
    )
)
