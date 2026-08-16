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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.ats.R
import com.iti.careerpilot.ats.presentation.coverletter.state.CoverLetterIntent
import com.iti.careerpilot.ats.presentation.coverletter.state.CoverLetterUiState
import com.iti.careerpilot.ats.presentation.coverletter.uimodels.CoverLetterContactInfo
import com.iti.careerpilot.ats.presentation.util.UiStateProvider
import com.iti.careerpilot.ats.presentation.util.rememberUiStateValue
import com.iti.careerpilot.ats.presentation.util.toPalette
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

val coverLetterPadding = 16.dp

@Composable
internal fun GeneratedCoverLetterCard(
    stateProvider: UiStateProvider<CoverLetterUiState>,
    onIntent: (CoverLetterIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    CareerPilotCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 8.dp),
        ) {
            CoverLetterCardHeader(
                stateProvider = stateProvider,
                onIntent = onIntent,
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            CoverLetterValue(
                stateProvider = stateProvider,
                onIntent = onIntent,
            )
            CoverLetterContacts(stateProvider = stateProvider)
        }
    }
}

@Composable
private fun CoverLetterCardHeader(
    stateProvider: UiStateProvider<CoverLetterUiState>,
    onIntent: (CoverLetterIntent) -> Unit,
) {
    val isEditing by rememberUiStateValue(stateProvider) { it.isEditing }
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
            label = stringResource(if (isEditing) R.string.ats_done else R.string.ats_edit),
            onClick = { onIntent(CoverLetterIntent.ToggleEditing) },
            contentDescription = stringResource(R.string.ats_cover_letter_edit_icon),
        )
        Spacer(modifier = Modifier.width(8.dp))
        CoverLetterActionButton(
            icon = Icons.Outlined.ContentCopy,
            label = stringResource(R.string.ats_copy),
            onClick = { onIntent(CoverLetterIntent.Copy) },
            contentDescription = stringResource(R.string.ats_cover_letter_copy_icon),
        )
    }
}

@Composable
private fun CoverLetterValue(
    stateProvider: UiStateProvider<CoverLetterUiState>,
    onIntent: (CoverLetterIntent) -> Unit,
) {
    val editor by rememberUiStateValue(stateProvider) {
        CoverLetterEditorState(isEditing = it.isEditing, value = it.editedValue)
    }
    if (editor.isEditing) {
        OutlinedTextField(
            value = editor.value,
            onValueChange = { onIntent(CoverLetterIntent.EditedValueChanged(it)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = coverLetterPadding),
            minLines = 12,
        )
    } else {
        Text(
            text = editor.value,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(horizontal = coverLetterPadding),
        )
    }
}

@Composable
private fun CoverLetterContacts(
    stateProvider: UiStateProvider<CoverLetterUiState>,
) {
    val values by rememberUiStateValue(stateProvider) {
        CoverLetterContactValues(
            name = it.contactName,
            email = it.contactEmail,
            phone = it.contactPhone,
        )
    }
    val contacts: ImmutableList<Pair<CoverLetterContactInfo, String>> = remember(values) {
        var result: PersistentList<Pair<CoverLetterContactInfo, String>> = persistentListOf()
        if (values.name.isNotBlank()) result = result.adding(CoverLetterContactInfo.NAME to values.name)
        if (values.email.isNotBlank()) result = result.adding(CoverLetterContactInfo.EMAIL to values.email)
        if (values.phone.isNotBlank()) result = result.adding(CoverLetterContactInfo.PHONE to values.phone)
        result
    }
    if (contacts.isEmpty()) return
    FlowRow(
        modifier = Modifier.padding(horizontal = coverLetterPadding),
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

@Immutable
private data class CoverLetterEditorState(
    val isEditing: Boolean,
    val value: String,
)

@Immutable
private data class CoverLetterContactValues(
    val name: String,
    val email: String,
    val phone: String,
)
