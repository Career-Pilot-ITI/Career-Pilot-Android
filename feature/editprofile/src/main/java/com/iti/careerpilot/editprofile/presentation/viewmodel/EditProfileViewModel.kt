package com.iti.careerpilot.editprofile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.editprofile.domain.models.RequestProfileUpdate
import com.iti.careerpilot.editprofile.domain.repo.EditProfileRepo
import com.iti.careerpilot.editprofile.presentation.action.EditProfileAction
import com.iti.careerpilot.editprofile.presentation.event.EditProfileEvent
import com.iti.careerpilot.editprofile.presentation.state.EditProfileState
import com.iti.common.result.onError
import com.iti.common.result.onSuccess
import com.iti.core.datastore.models.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val editProfileRepo: EditProfileRepo,
) : ViewModel() {

    private val _state = MutableStateFlow(EditProfileState())
    val state = _state.asStateFlow()

    private val _events = Channel<EditProfileEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var original: UserProfile = UserProfile()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val profile = editProfileRepo.userProfile.first()
            original = profile
            _state.update {
                it.copy(
                    displayName = profile.displayName,
                    username = profile.username,
                    email = profile.email,
                    gender = profile.gender,
                    dateOfBirth = profile.dateOfBirth,
                    targetRole = profile.targetRole,
                    industry = profile.industry,
                    experienceLevel = profile.experienceLevel,
                    trackName = profile.trackName,
                    currentJobTitle = profile.currentJobTitle,
                    yearsOfExperience = profile.yearsOfExperience.toString(),
                    skills = profile.skills,
                    targetCompanies = profile.targetCompanies,
                    educationLevel = profile.educationLevel,
                    timezone = profile.timezone,
                    avatarUrl = profile.avatarUrl,
                    avatarLocalUri = profile.avatarLocalUri,
                    cvUrl = profile.cvUrl,
                    cvFileName = profile.cvFileName,
                    cvFileSize = if (profile.cvSizeBytes > 0) "${profile.cvSizeBytes / 1024} KB" else "",
                    isLoading = false
                )
            }
        }
    }

    fun onAction(action: EditProfileAction) {
        when (action) {
            is EditProfileAction.OnDisplayNameChange ->
                _state.update { it.copy(displayName = action.value) }

            is EditProfileAction.OnUsernameChange ->
                _state.update {
                    it.copy(
                        username = action.value,
                        fieldErrors = it.fieldErrors - "username"
                    )
                }

            is EditProfileAction.OnEmailChange ->
                _state.update {
                    it.copy(
                        email = action.value,
                        fieldErrors = it.fieldErrors - "email"
                    )
                }

            is EditProfileAction.OnGenderChange ->
                _state.update { it.copy(gender = action.value) }

            is EditProfileAction.OnDateOfBirthChange ->
                _state.update { it.copy(dateOfBirth = action.value) }

            is EditProfileAction.OnTargetRoleChange ->
                _state.update { it.copy(targetRole = action.value) }

            is EditProfileAction.OnIndustryChange ->
                _state.update { it.copy(industry = action.value) }

            is EditProfileAction.OnExperienceLevelChange ->
                _state.update { it.copy(experienceLevel = action.value) }

            is EditProfileAction.OnTrackChange ->
                _state.update { it.copy(trackName = action.value) }

            is EditProfileAction.OnCurrentJobTitleChange ->
                _state.update { it.copy(currentJobTitle = action.value) }

            is EditProfileAction.OnYearsOfExperienceChange -> {
                if (action.value.isEmpty() || action.value.all { c -> c.isDigit() }) {
                    _state.update { it.copy(yearsOfExperience = action.value) }
                }
            }

            is EditProfileAction.OnEducationLevelChange ->
                _state.update { it.copy(educationLevel = action.value) }

            is EditProfileAction.OnTimezoneChange ->
                _state.update { it.copy(timezone = action.value) }

            is EditProfileAction.OnAvatarChange -> {
                action.value?.let { uri ->
                    _state.update { it.copy(isUploadingAvatar = true) }
                    viewModelScope.launch {
                        editProfileRepo.uploadImage(
                            uri = uri,
                            onProgress = { percent ->
                                _state.update { it.copy(avatarUploadProgress = percent) }
                            }
                        )
                            .onSuccess {
                                _state.update {
                                    it.copy(
                                        avatarLocalUri = uri.toString(),
                                        isUploadingAvatar = false,
                                        avatarUploadProgress = 0
                                    )
                                }
                            }
                            .onError {
                                _state.update {
                                    it.copy(
                                        isUploadingAvatar = false,
                                        avatarUploadProgress = 0
                                    )
                                }
                            }
                    }
                }
            }

            is EditProfileAction.OnCVUpload -> {
                action.value?.let { uri ->
                    _state.update { it.copy(isUploadingCV = true) }
                    viewModelScope.launch {
                        editProfileRepo.uploadCV(
                            uri = uri,
                            onProgress = { percent ->
                                _state.update { it.copy(cvUploadProgress = percent) }
                            }
                        )
                            .onSuccess { response ->
                                _state.update {
                                    it.copy(
                                        isUploadingCV = false,
                                        cvUrl = response.url,
                                        cvFileName = response.originalName,
                                        cvUploadProgress = 0
                                    )
                                }
                            }
                            .onError {
                                _state.update {
                                    it.copy(
                                        isUploadingCV = false,
                                        cvUploadProgress = 0
                                    )
                                }
                            }
                    }
                }
            }

            is EditProfileAction.OnSkillAdd -> {
                val skill = action.skill.trim()
                if (skill.isNotEmpty() && skill !in _state.value.skills) {
                    _state.update { it.copy(skills = it.skills + skill) }
                }
            }

            is EditProfileAction.OnSkillRemove ->
                _state.update { it.copy(skills = it.skills - action.skill) }

            is EditProfileAction.OnTargetCompanyAdd -> {
                val company = action.company.trim()
                if (company.isNotEmpty() && company !in _state.value.targetCompanies) {
                    _state.update { it.copy(targetCompanies = it.targetCompanies + company) }
                }
            }

            is EditProfileAction.OnTargetCompanyRemove ->
                _state.update { it.copy(targetCompanies = it.targetCompanies - action.company) }

            EditProfileAction.OnBackClick ->
                sendEvent(EditProfileEvent.NavigateBack)

            EditProfileAction.OnSaveClick -> save()
        }
    }

    private fun save() {
        val current = _state.value

        val errors = mutableMapOf<String, String>()
        if (current.username.isBlank()) errors["username"] = "Username can't be empty"
        if (current.email.isNotBlank() && !current.email.contains("@")) errors["email"] =
            "Enter a valid email"

        if (errors.isNotEmpty()) {
            _state.update { it.copy(fieldErrors = errors) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, fieldErrors = emptyMap()) }
            val request = RequestProfileUpdate(
                username = current.username.takeIf { it != original.username },
                email = current.email.takeIf { it != original.email },
                displayName = current.displayName.takeIf { it != original.displayName },
                gender = current.gender.takeIf { it != original.gender },
                dateOfBirth = current.dateOfBirth.takeIf { it != original.dateOfBirth },
                targetRole = current.targetRole.takeIf { it != original.targetRole },
                industry = current.industry.takeIf { it != original.industry },
                experienceLevel = current.experienceLevel.takeIf { it != original.experienceLevel },
                currentJobTitle = current.currentJobTitle.takeIf { it != original.currentJobTitle },
                yearsOfExperience = current.yearsOfExperience.toIntOrNull()
                    ?.takeIf { it != original.yearsOfExperience },
                skills = current.skills.takeIf { it != original.skills },
                targetCompanies = current.targetCompanies.takeIf { it != original.targetCompanies },
                educationLevel = current.educationLevel.takeIf { it != original.educationLevel },
                timezone = current.timezone.takeIf { it != original.timezone },
            )

            editProfileRepo.updateProfile(request)
                .onSuccess {
                    _state.update { it.copy(isLoading = false) }
                    _events.send(EditProfileEvent.NavigateBack)
                }
                .onError {
                    _state.update {
                        it.copy(isLoading = false)
                    }
                }
        }
    }

    private fun sendEvent(event: EditProfileEvent) {
        viewModelScope.launch { _events.send(event) }
    }
}