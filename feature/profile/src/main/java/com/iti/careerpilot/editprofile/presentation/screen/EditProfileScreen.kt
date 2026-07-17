package com.iti.careerpilot.editprofile.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.common.ObserveEvent
import com.iti.careerpilot.core.designsystem.CareerPilotTheme
import com.iti.careerpilot.editprofile.presentation.action.EditProfileAction
import com.iti.careerpilot.editprofile.presentation.event.EditProfileEvent
import com.iti.careerpilot.editprofile.presentation.screen.components.ChipInputField
import com.iti.careerpilot.editprofile.presentation.screen.components.EditProfileHeader
import com.iti.careerpilot.editprofile.presentation.screen.components.FormSection
import com.iti.careerpilot.editprofile.presentation.screen.components.LabeledDropdownField
import com.iti.careerpilot.editprofile.presentation.screen.components.LabeledTextField
import com.iti.careerpilot.editprofile.presentation.screen.components.LoadingDialog
import com.iti.careerpilot.editprofile.presentation.screen.components.SaveBar
import com.iti.careerpilot.editprofile.presentation.screen.models.EducationLevel
import com.iti.careerpilot.editprofile.presentation.screen.models.ExperienceLevel
import com.iti.careerpilot.editprofile.presentation.screen.models.Gender
import com.iti.careerpilot.editprofile.presentation.state.EditProfileState
import com.iti.careerpilot.editprofile.presentation.viewmodel.EditProfileViewModel
import com.iti.careerpilot.profile.R

@Composable
fun EditProfileRoot(
    navigateBack: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    ObserveEvent(viewModel.events) { event ->
        when (event) {
            EditProfileEvent.NavigateBack -> navigateBack()
            is EditProfileEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
        }
    }

    EditProfileScreen(
        state = state,
        onAction = viewModel::onAction,
        onBack = navigateBack,
        snackbarHostState = snackbarHostState,
    )
    if (state.isLoading) {
        LoadingDialog()
    }
}

@Composable
fun EditProfileScreen(
    state: EditProfileState,
    onAction: (EditProfileAction) -> Unit,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.edit_profile)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data -> Snackbar(snackbarData = data) }
        },
        bottomBar = {
            if (!state.isLoading) {
                SaveBar(
                    isSaving = state.isSaving,
                    onSave = { onAction(EditProfileAction.OnSaveClick) },
                    onCancel = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            item {
                EditProfileHeader(
                    displayName = state.displayName,
                    username = state.username,
                    avatarUri = state.avatarUrl,
                    onAvatarChange = { uri ->
                        onAction(
                            EditProfileAction.OnAvatarChange(
                                uri
                            )
                        )
                    }
                )
            }

            item {
                FormSection(
                    title = stringResource(R.string.basic_info),
                    icon = ImageVector.vectorResource(R.drawable.ic_account)
                ) {
                    LabeledTextField(
                        label = stringResource(R.string.display_name),
                        value = state.displayName,
                        onValueChange = {
                            onAction(
                                EditProfileAction.OnDisplayNameChange(
                                    it
                                )
                            )
                        }
                    )
                    LabeledTextField(
                        label = stringResource(R.string.username_placeholder),
                        value = state.username,
                        onValueChange = {
                            onAction(
                                EditProfileAction.OnUsernameChange(
                                    it
                                )
                            )
                        },
                        errorText = state.fieldErrors["username"]
                    )
                    LabeledTextField(
                        label = stringResource(R.string.email),
                        value = state.email,
                        onValueChange = {
                            onAction(
                                EditProfileAction.OnEmailChange(
                                    it
                                )
                            )
                        },
                        keyboardType = KeyboardType.Email,
                        errorText = state.fieldErrors["email"]
                    )
                    LabeledDropdownField(
                        label = stringResource(R.string.gender),
                        value = state.gender,
                        options = Gender.entries.map {
                            stringResource(
                                it.labelRes
                            )
                        },
                        onValueChange = {
                            onAction(
                                EditProfileAction.OnGenderChange(
                                    it
                                )
                            )
                        }
                    )
                    LabeledTextField(
                        label = stringResource(R.string.date_of_birth),
                        value = state.dateOfBirth,
                        placeholder = stringResource(R.string.yyyy_mm_dd),
                        onValueChange = {
                            onAction(
                                EditProfileAction.OnDateOfBirthChange(
                                    it
                                )
                            )
                        }
                    )
                }
            }

            item {
                FormSection(
                    title = stringResource(R.string.career),
                    icon = ImageVector.vectorResource(R.drawable.ic_work)
                ) {
                    LabeledTextField(
                        label = stringResource(R.string.target_role),
                        value = state.targetRole,
                        onValueChange = {
                            onAction(
                                EditProfileAction.OnTargetRoleChange(
                                    it
                                )
                            )
                        }
                    )
                    LabeledTextField(
                        label = stringResource(R.string.industry),
                        value = state.industry,
                        onValueChange = {
                            onAction(
                                EditProfileAction.OnIndustryChange(
                                    it
                                )
                            )
                        }
                    )
                    LabeledDropdownField(
                        label = stringResource(R.string.experience_level),
                        value = state.experienceLevel,
                        options = ExperienceLevel.entries.map {
                            stringResource(
                                it.labelRes
                            )
                        },
                        onValueChange = {
                            onAction(
                                EditProfileAction.OnExperienceLevelChange(
                                    it
                                )
                            )
                        }
                    )
                    LabeledTextField(
                        label = stringResource(R.string.current_job_title),
                        value = state.currentJobTitle,
                        onValueChange = {
                            onAction(
                                EditProfileAction.OnCurrentJobTitleChange(
                                    it
                                )
                            )
                        }
                    )
                    LabeledTextField(
                        label = stringResource(R.string.years_of_experience),
                        value = state.yearsOfExperience,
                        onValueChange = {
                            onAction(
                                EditProfileAction.OnYearsOfExperienceChange(
                                    it
                                )
                            )
                        },
                        keyboardType = KeyboardType.Number
                    )
                    LabeledDropdownField(
                        label = stringResource(R.string.education_level),
                        value = state.educationLevel,
                        options = EducationLevel.entries.map {
                            stringResource(
                                it.labelRes
                            )
                        },
                        onValueChange = {
                            onAction(
                                EditProfileAction.OnEducationLevelChange(
                                    it
                                )
                            )
                        }
                    )
                }
            }

            item {
                FormSection(
                    title = stringResource(R.string.skills),
                    icon = ImageVector.vectorResource(R.drawable.ic_star)
                ) {
                    ChipInputField(
                        chips = state.skills,
                        placeholder = stringResource(R.string.add_a_skill_and_press_enter),
                        onAdd = {
                            onAction(
                                EditProfileAction.OnSkillAdd(
                                    it
                                )
                            )
                        },
                        onRemove = {
                            onAction(
                                EditProfileAction.OnSkillRemove(
                                    it
                                )
                            )
                        }
                    )
                }
            }

            item {
                FormSection(
                    title = stringResource(R.string.target_companies),
                    icon = ImageVector.vectorResource(R.drawable.ic_business)
                ) {
                    ChipInputField(
                        chips = state.targetCompanies,
                        placeholder = stringResource(R.string.add_a_company_and_press_enter),
                        onAdd = {
                            onAction(
                                EditProfileAction.OnTargetCompanyAdd(
                                    it
                                )
                            )
                        },
                        onRemove = {
                            onAction(
                                EditProfileAction.OnTargetCompanyRemove(
                                    it
                                )
                            )
                        }
                    )
                }
            }

            item { Spacer(Modifier.height(12.dp)) }
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun EditProfileScreenPreview() {
    CareerPilotTheme {
        EditProfileScreen(
            state = EditProfileState(
                displayName = "Amina Hassan",
                username = "amina.h",
                email = "amina@example.com",
                targetRole = "Senior Product Designer",
                industry = "Technology",
                experienceLevel = "Mid-level",
                skills = listOf("Figma", "User Research", "Prototyping"),
                targetCompanies = listOf("Google", "Airbnb")
            ),
            onAction = {},
            onBack = {}
        )
    }
}