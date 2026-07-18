package com.iti.careerpilot.profile.presentation.screen.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
import com.iti.careerpilot.profile.R
import com.iti.core.datastore.models.UserProfile


@Composable
fun InfoCard(
    profile: UserProfile,
) {
    CareerPilotCard(
        elevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.profile_info),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            InfoRow(stringResource(R.string.email), profile.email.ifBlank { stringResource(R.string.not_set) })
            InfoRow(stringResource(R.string.phone), profile.phoneNumber.ifBlank { stringResource(R.string.not_set) })
            InfoRow(stringResource(R.string.target_role), profile.targetRole.ifBlank { stringResource(R.string.not_set) })
            InfoRow(stringResource(R.string.industry), profile.industry.ifBlank { stringResource(R.string.not_set) })
            InfoRow(
                stringResource(R.string.experience),
                profile.experienceLevel.ifBlank { stringResource(R.string.not_set) },
                showDivider = false
            )
        }
    }
}
