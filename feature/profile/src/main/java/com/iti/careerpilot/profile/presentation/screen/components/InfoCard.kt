package com.iti.careerpilot.profile.presentation.screen.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
import com.iti.careerpilot.profile.R
import com.iti.core.datastore.models.UserProfile


@Composable
fun InfoCard(
    profile: UserProfile,
    onEditCareerClick: () -> Unit
) {
    CareerPilotCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.career_info),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = onEditCareerClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_edit),
                        contentDescription = stringResource(R.string.edit_profile),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
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
}
