package com.iti.careerpilot.profile.presentation.screen.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
import com.iti.careerpilot.profile.R
import com.iti.core.datastore.models.UserProfile


@Composable
fun InfoCard(profile: UserProfile) {
    CareerPilotCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(4.dp)) {
            InfoRow(
                label = stringResource(R.string.email),
                value = profile.email.ifBlank { stringResource(R.string.not_set) }
            )
            InfoRow(
                label = stringResource(R.string.phone),
                value = profile.phoneNumber.ifBlank { stringResource(R.string.not_set) }
            )
            InfoRow(
                label = stringResource(R.string.target_role),
                value = profile.targetRole.ifBlank { stringResource(R.string.not_set) }
            )
            InfoRow(
                label = stringResource(R.string.industry),
                value = profile.industry.ifBlank { stringResource(R.string.not_set) }
            )
            InfoRow(
                label = stringResource(R.string.experience),
                value = profile.experienceLevel.ifBlank { stringResource(R.string.not_set) },
                showDivider = false
            )
        }
    }
}