package com.iti.careerpilot.profile.presentation.screen.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.core.designsystem.CareerPilotPalette
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
import com.iti.careerpilot.profile.R


@Composable
fun MenuSection(
    onOpenSettings: () -> Unit,
    onLogOut: () -> Unit
) {
    CareerPilotCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(4.dp)
        ) {
            MenuRow(
                icon = ImageVector.vectorResource(id = R.drawable.ic_settings),
                label = stringResource(R.string.settings),
                onClick = onOpenSettings
            )
            androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            MenuRow(
                icon = ImageVector.vectorResource(id = R.drawable.ic_logout),
                label = stringResource(R.string.logout),
                tint = CareerPilotPalette.coral,
                showChevron = false,
                onClick = onLogOut
            )
        }
    }
}
