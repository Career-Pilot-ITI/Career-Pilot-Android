package com.iti.onboarding.presentation.screen.profileinfo.view.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.iti.onboarding.R
import kotlinx.collections.immutable.toImmutableList

import com.iti.onboarding.presentation.screen.profileinfo.model.ExperienceLevel

@Composable
fun ProfileInfoForm(
    name: String,
    onNameChanged: (String) -> Unit,
    email: String,
    onEmailChanged: (String) -> Unit,
    isEmailInvalid: Boolean,
    title: String,
    onTitleChanged: (String) -> Unit,
    experience: String,
    onExperienceChanged: (String) -> Unit,
    focusManager: FocusManager,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            ProfileFieldRow(
                icon = Icons.Default.Person,
                label = stringResource(R.string.profile_info_full_name_label),
                value = name,
                onValueChange = onNameChanged,
                placeholder = stringResource(R.string.profile_info_full_name_placeholder),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            
            ProfileFieldRow(
                icon = Icons.Default.Email,
                label = stringResource(R.string.profile_info_email_label),
                value = email,
                onValueChange = onEmailChanged,
                placeholder = stringResource(R.string.profile_info_email_placeholder),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                isError = isEmailInvalid,
                errorMessage = if (isEmailInvalid) stringResource(R.string.profile_info_email_invalid) else null
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            
            ProfileFieldRow(
                icon = Icons.Default.Work,
                label = stringResource(R.string.profile_info_title_role_label),
                value = title,
                onValueChange = onTitleChanged,
                placeholder = stringResource(R.string.profile_info_title_role_placeholder),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            
            ProfileDropdownRow(
                icon = Icons.Default.Assessment,
                label = stringResource(R.string.profile_info_experience_level_label),
                value = experience,
                options = ExperienceLevel.entries.map { stringResource(it.labelRes) }.toImmutableList(),
                onValueChange = onExperienceChanged,
                placeholder = stringResource(R.string.profile_info_experience_level_placeholder)
            )
        }
    }
}
