package com.iti.careerpilot.profile.presentation.screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.core.designsystem.CareerPilotPalette
import com.iti.careerpilot.profile.R
import com.iti.core.datastore.models.UserProfile


@Composable
fun StatsRow(profile: UserProfile) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatPill(
            modifier = Modifier.weight(1f),
            icon = ImageVector.vectorResource(id = R.drawable.ic_monetization),
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            iconTint = MaterialTheme.colorScheme.primary,
            label = stringResource(R.string.coins),
            value = profile.coinBalance.toString()
        )
        StatPill(
            modifier = Modifier.weight(1f),
            icon = ImageVector.vectorResource(id = R.drawable.ic_workspace_premium),
            containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
            iconTint = MaterialTheme.colorScheme.secondary,
            label = stringResource(R.string.plan),
            value = profile.subscriptionTier.ifBlank { stringResource(R.string.free) }
        )
    }
}