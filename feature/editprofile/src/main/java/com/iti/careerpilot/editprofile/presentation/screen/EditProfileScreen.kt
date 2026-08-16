package com.iti.careerpilot.editprofile.presentation.screen

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.core.designsystem.CareerPilotTheme
import com.iti.careerpilot.core.designsystem.common.ObserveEvent
import com.iti.careerpilot.core.designsystem.components.BackIconButton
import com.iti.careerpilot.editprofile.R
import com.iti.careerpilot.editprofile.presentation.action.EditProfileAction
import com.iti.careerpilot.editprofile.presentation.event.EditProfileEvent
import com.iti.careerpilot.editprofile.presentation.screen.components.CVUploadField
import com.iti.careerpilot.editprofile.presentation.screen.components.ChipInputField
import com.iti.careerpilot.editprofile.presentation.screen.components.EditProfileHeader
import com.iti.careerpilot.editprofile.presentation.screen.components.FormSection
import com.iti.careerpilot.editprofile.presentation.screen.components.LabeledDropdownField
import com.iti.careerpilot.editprofile.presentation.screen.components.LabeledTextField
import com.iti.careerpilot.core.designsystem.components.LoadingDialog
import com.iti.careerpilot.core.designsystem.components.TracksFlow
import com.iti.careerpilot.core.designsystem.components.UploadProgressDialog
import com.iti.careerpilot.editprofile.presentation.screen.components.SaveBar
import com.iti.careerpilot.editprofile.presentation.screen.models.EducationLevel
import com.iti.careerpilot.editprofile.presentation.screen.models.ExperienceLevel
import com.iti.careerpilot.editprofile.presentation.screen.models.Gender
import com.iti.careerpilot.editprofile.presentation.state.EditProfileState
import com.iti.careerpilot.editprofile.presentation.viewmodel.EditProfileViewModel
import com.iti.common.model.ProfileEditSection
import java.time.LocalDate

@Composable
fun EditProfileRoot(
    section: ProfileEditSection,
    navigateBack: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.onAction(EditProfileAction.Initial)
    }

    ObserveEvent(viewModel.events) { event ->
        when (event) {
            EditProfileEvent.NavigateBack -> navigateBack()
            is EditProfileEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
        }
    }

    EditProfileScreen(
        state = state,
        section = section,
        onAction = viewModel::onAction,
        onBack = navigateBack,
        snackbarHostState = snackbarHostState,
    )
    if (state.isLoading) {
        LoadingDialog()
    }

    if (state.isUploadingAvatar) {
        UploadProgressDialog(
            progress = state.avatarUploadProgress,
            title = stringResource(R.string.uploading)
        )
    }

    if (state.isUploadingCV) {
        val isParsing = state.isAnalyzingCV || state.cvUploadProgress >= 100
        UploadProgressDialog(
            progress = state.cvUploadProgress,
            isParsing = isParsing,
            title = stringResource(if (isParsing) R.string.parsing_cv else R.string.uploading)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    state: EditProfileState,
    section: ProfileEditSection,
    onAction: (EditProfileAction) -> Unit,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    var showDatePicker by remember { mutableStateOf(false) }

    val selectableDates = remember {
        val currentTime = System.currentTimeMillis()
        val currentYear = LocalDate.now().year
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= currentTime
            }

            override fun isSelectableYear(year: Int): Boolean {
                return year <= currentYear
            }
        }
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = state.dateOfBirthMillis,
        selectableDates = selectableDates
    )
    val dateInteractionSource = remember { MutableInteractionSource() }
    LaunchedEffect(dateInteractionSource) {
        dateInteractionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) {
                showDatePicker = true
            }
        }
    }

    LaunchedEffect(state.dateOfBirthMillis) {
        if (datePickerState.selectedDateMillis != state.dateOfBirthMillis) {
            datePickerState.selectedDateMillis = state.dateOfBirthMillis
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(section.titleRes),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    BackIconButton(onBack = onBack)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data -> Snackbar(snackbarData = data) }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(top = 8.dp)
            ) {

                if (section == ProfileEditSection.ALL || section == ProfileEditSection.PERSONAL) {
                    item {
                        EditProfileHeader(
                            displayName = state.displayName,
                            username = state.username,
                            avatarUri = state.avatarLocalUri,
                            onAvatarChange = { uri -> onAction(EditProfileAction.OnAvatarChange(uri)) }
                        )
                    }

                    item {
                        FormSection(
                            title = stringResource(R.string.personal_info),
                            icon = ImageVector.vectorResource(R.drawable.ic_account)
                        ) {
                            LabeledTextField(
                                label = stringResource(R.string.display_name),
                                value = state.displayName,
                                leadingIcon = ImageVector.vectorResource(R.drawable.ic_account),
                                onValueChange = { onAction(EditProfileAction.OnDisplayNameChange(it)) },
                                errorText = if (state.displayNameError) stringResource(R.string.error_empty_display_name) else null
                            )
                            LabeledTextField(
                                label = stringResource(R.string.email),
                                value = state.email,
                                leadingIcon = ImageVector.vectorResource(R.drawable.ic_email),
                                onValueChange = { onAction(EditProfileAction.OnEmailChange(it)) },
                                keyboardType = KeyboardType.Email,
                                errorText = if (state.emailError) stringResource(R.string.error_invalid_email) else null
                            )
                            val genderOptions = Gender.entries.map { stringResource(it.labelRes) }
                            LabeledDropdownField(
                                label = stringResource(R.string.gender),
                                value = Gender.fromBackendValue(state.gender)
                                    ?.let { stringResource(it.labelRes) } ?: state.gender,
                                leadingIcon = ImageVector.vectorResource(R.drawable.ic_person),
                                options = genderOptions,
                                onValueChange = { selectedLabel ->
                                    val index = genderOptions.indexOf(selectedLabel)
                                    if (index != -1) {
                                        onAction(EditProfileAction.OnGenderChange(Gender.entries[index].backendValue))
                                    }
                                }
                            )

                            LabeledTextField(
                                label = stringResource(R.string.date_of_birth),
                                value = state.dateOfBirthDisplay,
                                leadingIcon = ImageVector.vectorResource(R.drawable.ic_edit_calendar),
                                placeholder = stringResource(R.string.yyyy_mm_dd),
                                onValueChange = { },
                                readOnly = true,
                                interactionSource = dateInteractionSource
                            )

                        }
                    }
                }

                if (section == ProfileEditSection.ALL || section == ProfileEditSection.CAREER) {
                    item {
                        FormSection(
                            title = stringResource(R.string.career),
                            icon = ImageVector.vectorResource(R.drawable.ic_work)
                        ) {
                            LabeledTextField(
                                label = stringResource(R.string.target_role),
                                value = state.targetRole,
                                leadingIcon = ImageVector.vectorResource(R.drawable.ic_flag),
                                onValueChange = { onAction(EditProfileAction.OnTargetRoleChange(it)) }
                            )
                            LabeledTextField(
                                label = stringResource(R.string.industry),
                                value = state.industry,
                                leadingIcon = ImageVector.vectorResource(R.drawable.ic_business),
                                onValueChange = { onAction(EditProfileAction.OnIndustryChange(it)) }
                            )
                            LabeledDropdownField(
                                label = stringResource(R.string.experience_level),
                                value = state.experienceLevel,
                                leadingIcon = ImageVector.vectorResource(R.drawable.ic_briefcase),
                                options = ExperienceLevel.entries.map { stringResource(it.labelRes) },
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
                                leadingIcon = ImageVector.vectorResource(R.drawable.ic_work),
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
                                leadingIcon = ImageVector.vectorResource(R.drawable.ic_calendar),
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
                                leadingIcon = ImageVector.vectorResource(R.drawable.ic_school),
                                options = EducationLevel.entries.map { stringResource(it.labelRes) },
                                onValueChange = {
                                    onAction(
                                        EditProfileAction.OnEducationLevelChange(
                                            it
                                        )
                                    )
                                }
                            )
                            CVUploadField(
                                fileName = state.cvFileName,
                                isUploading = state.isUploadingCV,
                                isParsing = state.isAnalyzingCV,
                                uploadProgress = state.cvUploadProgress,
                                onCVSelected = { uri -> onAction(EditProfileAction.OnCVUpload(uri)) },
                                onCVRemove = { onAction(EditProfileAction.OnCVRemove) }
                            )
                        }
                    }

                    item {
                        FormSection(
                            title = stringResource(R.string.skills),
                            icon = ImageVector.vectorResource(R.drawable.ic_star)
                        ) {
                            ChipInputField(
                                label = stringResource(R.string.skills),
                                chips = state.skills,
                                placeholder = stringResource(R.string.add_skills),
                                leadingIcon = ImageVector.vectorResource(R.drawable.ic_star),
                                onAdd = { onAction(EditProfileAction.OnSkillAdd(it)) },
                                onRemove = { onAction(EditProfileAction.OnSkillRemove(it)) }
                            )
                        }
                    }

                    item {
                        FormSection(
                            title = stringResource(R.string.target_companies),
                            icon = ImageVector.vectorResource(R.drawable.ic_company)
                        ) {
                            ChipInputField(
                                label = stringResource(R.string.target_companies),
                                chips = state.targetCompanies,
                                placeholder = stringResource(R.string.add_a_company_and_press_enter),
                                leadingIcon = ImageVector.vectorResource(R.drawable.ic_business),
                                onAdd = { onAction(EditProfileAction.OnTargetCompanyAdd(it)) },
                                onRemove = { onAction(EditProfileAction.OnTargetCompanyRemove(it)) }
                            )
                        }
                    }
                }

                if ((section == ProfileEditSection.ALL || section == ProfileEditSection.CAREER) && state.tracks.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.track),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        TracksFlow(
                            items = state.tracks,
                            selectedItem = state.tracks.find { it.name == state.trackName },
                            onItemClick = { onAction(EditProfileAction.OnTrackChange(it)) },
                            labelProvider = { it.name },
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                item { Spacer(Modifier.height(4.dp)) }
            }

            SaveBar(
                isSaving = state.isLoading,
                onSave = { onAction(EditProfileAction.OnSaveClick(section)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            )
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            onAction(EditProfileAction.OnDateSelected(millis))
                        }
                        showDatePicker = false
                    },
                    modifier = Modifier.width(100.dp)
                ) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        containerColor = Color.Transparent
                    ),
                    modifier = Modifier.width(100.dp)
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
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
            section = ProfileEditSection.ALL,
            onAction = {},
            onBack = {}
        )
    }
}
