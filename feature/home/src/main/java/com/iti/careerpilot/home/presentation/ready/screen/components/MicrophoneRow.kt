package com.iti.careerpilot.home.presentation.ready.screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.core.designsystem.CareerPilotPalette
import com.iti.careerpilot.core.designsystem.CareerPilotShapes
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
import com.iti.careerpilot.home.R

@Composable
fun MicrophoneRow(
    isGranted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CareerPilotCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .clickable(enabled = !isGranted, onClick = onClick)
                .padding(Dimens.CardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceL),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CareerPilotShapes.small)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Mic,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.ready_mic_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(
                        if (isGranted) R.string.ready_mic_granted else R.string.ready_mic_required
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isGranted) CareerPilotPalette.green
                    else MaterialTheme.colorScheme.error,
                )
            }

            if (isGranted) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = CareerPilotPalette.green,
                )
            }
        }
    }
}

