package com.iti.careerpilot.editprofile.presentation.screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.core.designsystem.components.ButtonVariant
import com.iti.careerpilot.core.designsystem.components.CareerPilotButton
import com.iti.careerpilot.editprofile.R


@Composable
fun SaveBar(
    isSaving: Boolean,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
    ) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CareerPilotButton(
                text = if (isSaving) stringResource(R.string.saving) else stringResource(R.string.save_changes),
                onClick = onSave,
                enabled = !isSaving,
                variant = ButtonVariant.PRIMARY,
                modifier = Modifier.weight(1f)
            )
        }
    }
}