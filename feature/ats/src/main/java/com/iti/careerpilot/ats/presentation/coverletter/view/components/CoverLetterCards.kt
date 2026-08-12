package com.iti.careerpilot.ats.presentation.coverletter.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.ats.R
import com.iti.careerpilot.ats.presentation.coverletter.state.CoverLetterAction
import com.iti.careerpilot.ats.presentation.coverletter.state.CoverLetterUiState
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard

@Composable
internal fun GeneratedCoverLetterCard(
    state: CoverLetterUiState,
    onAction: (CoverLetterAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    CareerPilotCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.cover_letter),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = { onAction(CoverLetterAction.ToggleEditing) }) {
                    Text(
                        stringResource(
                            if (state.isEditing) R.string.ats_done else R.string.ats_edit,
                        ),
                    )
                }
                TextButton(onClick = { onAction(CoverLetterAction.Copy) }) {
                    Text(stringResource(R.string.ats_copy))
                }
            }
            if (state.isEditing) {
                OutlinedTextField(
                    value = state.editedValue,
                    onValueChange = { onAction(CoverLetterAction.EditedValueChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 12,
                )
            } else {
                Text(
                    text = state.editedValue,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            val contacts = listOf(state.contactName, state.contactEmail, state.contactPhone)
                .filter(String::isNotBlank)
            if (contacts.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    contacts.forEach { contact ->
                        AssistChip(
                            onClick = {},
                            label = { Text(contact) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun ApproachTipsCard(
    tips: String,
    modifier: Modifier = Modifier,
) {
    CareerPilotCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.ats_approach_tips),
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = tips,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}
