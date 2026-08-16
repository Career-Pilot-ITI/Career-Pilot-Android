package com.iti.careerpilot.profile.presentation.screen.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
import com.iti.careerpilot.profile.R


@Composable
fun MenuSection(
    onEditPersonalInfoClick: () -> Unit,
    onEditCareerClick: () -> Unit,
    onOpenSettings: () -> Unit,
    onLogOut: () -> Unit,
) {
    CareerPilotCard(
        elevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            MenuRow(
                icon = ImageVector.vectorResource(id = R.drawable.ic_person),
                label = stringResource(R.string.personal_info),
                onClick = onEditPersonalInfoClick
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            MenuRow(
                icon = ImageVector.vectorResource(id = R.drawable.ic_business),
                label = stringResource(R.string.career_info),
                onClick = onEditCareerClick
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            MenuRow(
                icon = ImageVector.vectorResource(id = R.drawable.ic_settings),
                label = stringResource(R.string.settings),
                onClick = onOpenSettings
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            MenuRow(
                icon = ImageVector.vectorResource(id = R.drawable.ic_logout),
                label = stringResource(R.string.logout),
                tint = MaterialTheme.colorScheme.error,
                showChevron = false,
                onClick = onLogOut
            )
        }
    }
}

