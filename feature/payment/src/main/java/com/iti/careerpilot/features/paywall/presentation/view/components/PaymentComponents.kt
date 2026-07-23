package com.iti.careerpilot.features.paywall.presentation.view.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.iti.careerpilot.core.designsystem.CareerPilotTypography
import com.iti.careerpilot.core.designsystem.Dimens

@Composable
fun ScreenTitle(
    title: String,
    modifier: Modifier = Modifier,
    color: Color? = null
) {
    Text(
        text = title,
        modifier = modifier,
        style = MaterialTheme.typography.headlineMedium,
        color = color ?: MaterialTheme.colorScheme.onBackground
    )
}

@Composable
fun ActionButton(
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(Dimens.SpaceS))
        Text(text = label, style = CareerPilotTypography.labelLarge)
    }
}
