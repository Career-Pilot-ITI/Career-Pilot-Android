package com.iti.careerpilot.ats.presentation.entry.view.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.ats.R
import com.iti.careerpilot.ats.presentation.entry.state.AtsEntryAction
import com.iti.careerpilot.ats.presentation.entry.state.AtsEntryUiState
import com.iti.careerpilot.core.designsystem.softShadow

@Composable
fun JobUrlTextField(
    state: AtsEntryUiState,
    onAction: (AtsEntryAction) -> Unit,
    jobUrlDescription: String
) {
    OutlinedTextField(
        value = state.jobUrl,
        onValueChange = { onAction(AtsEntryAction.JobUrlChanged(it)) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .semantics { contentDescription = jobUrlDescription }
            .clip(RoundedCornerShape(12.dp))
            .softShadow(elevation = 8.dp, shape = RoundedCornerShape(12.dp)),
        placeholder = {
            Text(
                stringResource(R.string.ats_job_url_hint),
                style = MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                )
            )
        },
        singleLine = true,
        enabled = !state.isBusy,
        isError = state.jobUrl.isNotBlank() && !state.isUrlValid,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
        prefix = {
            Row {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                    contentDescription = stringResource(R.string.ats_open_job)
                )
                Spacer(Modifier.width(8.dp))
            }
        },
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors().copy(
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = MaterialTheme.colorScheme.surface,
            errorContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = MaterialTheme.colorScheme.surface,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.surface,
            disabledIndicatorColor = MaterialTheme.colorScheme.surface,
            errorIndicatorColor = MaterialTheme.colorScheme.surface,
        )
    )
}