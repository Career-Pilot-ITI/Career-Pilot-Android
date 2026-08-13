package com.iti.careerpilot.home.presentation.ready.screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.home.R

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LandmarkFeatureChips(
    enablePosture: Boolean,
    enableHands: Boolean,
    onPostureToggle: (Boolean) -> Unit,
    onHandsToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceS)
    ) {
        Text(
            text = stringResource(R.string.ready_analysis_features),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceS),
        ) {
            // Face — always enabled, not toggleable
            FilterChip(
                selected = true,
                onClick = { /* always selected */ },
                label = { Text(stringResource(R.string.ready_feature_face)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Face,
                        contentDescription = null,
                    )
                },
                enabled = false,
            )
            // Posture
            FilterChip(
                selected = enablePosture,
                onClick = { onPostureToggle(!enablePosture) },
                label = { Text(stringResource(R.string.ready_feature_posture)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Accessibility,
                        contentDescription = null,
                    )
                },
            )
            // Hands
            FilterChip(
                selected = enableHands,
                onClick = { onHandsToggle(!enableHands) },
                label = { Text(stringResource(R.string.ready_feature_hands)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = null,
                    )
                },
            )
        }
    }
}
