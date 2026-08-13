package com.iti.careerpilot.ats.presentation.coverletter.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.ats.R
import com.iti.careerpilot.ats.presentation.coverletter.state.CoverLetterAction
import com.iti.careerpilot.ats.presentation.coverletter.state.CoverLetterUiState
import com.iti.careerpilot.ats.presentation.coverletter.uimodels.CoverLetterContactInfo
import com.iti.careerpilot.ats.presentation.util.toPalette
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard

val coverLetterPadding = 16.dp

@Composable
internal fun GeneratedCoverLetterCard(
    state: CoverLetterUiState,
    onAction: (CoverLetterAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val contacts = remember(
        state.contactName,
        state.contactEmail,
        state.contactPhone,
    ) {
        listOf(
            CoverLetterContactInfo.NAME to state.contactName,
            CoverLetterContactInfo.EMAIL to state.contactEmail,
            CoverLetterContactInfo.PHONE to state.contactPhone,
        ).filter { it.second.isNotBlank() }
    }

    CareerPilotCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 8.dp),
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = coverLetterPadding, vertical = 4.dp),
            ) {

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_file_text),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outlineVariant,
                    )

                    Text(
                        text = stringResource(R.string.cover_letter),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                }

                CoverLetterActionButton(
                    icon = Icons.Outlined.Edit,
                    label = stringResource(
                        if (state.isEditing) {
                            R.string.ats_done
                        } else {
                            R.string.ats_edit
                        }
                    ),
                    onClick = {
                        onAction(CoverLetterAction.ToggleEditing)
                    },
                    contentDescription = stringResource(
                        R.string.ats_cover_letter_edit_icon
                    ),
                )

                Spacer(modifier = Modifier.width(8.dp))

                CoverLetterActionButton(
                    icon = Icons.Outlined.ContentCopy,
                    label = stringResource(R.string.ats_copy),
                    onClick = {
                        onAction(CoverLetterAction.Copy)
                    },
                    contentDescription = stringResource(
                        R.string.ats_cover_letter_copy_icon
                    ),
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant
            )

            if (state.isEditing) {
                OutlinedTextField(
                    value = state.editedValue,
                    onValueChange = {
                        onAction(
                            CoverLetterAction.EditedValueChanged(it)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = coverLetterPadding),
                    minLines = 12,
                )
            } else {
                Text(
                    text = state.editedValue,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(
                        horizontal = coverLetterPadding
                    ),
                )
            }

            if (contacts.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.padding(
                        horizontal = coverLetterPadding
                    ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    contacts.forEach { (type, value) ->
                        CoverLetterContactInfoCard(
                            palette = type.toPalette(),
                            info = value,
                        )
                    }
                }
            }
        }
    }
}