package com.iti.careerpilot.editprofile.presentation.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.common.model.ProfileEditSection
import com.iti.careerpilot.editprofile.data.datasource.remote.models.FileUploadResponse
import com.iti.careerpilot.editprofile.domain.models.RequestProfileUpdate
import com.iti.careerpilot.editprofile.domain.repo.EditProfileRepo
import com.iti.careerpilot.editprofile.presentation.action.EditProfileAction
import com.iti.careerpilot.editprofile.presentation.event.EditProfileEvent
import com.iti.careerpilot.editprofile.presentation.state.EditProfileState
import com.iti.common.dispatcher.CareerPilotDispatchers
import com.iti.common.dispatcher.Dispatcher
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.common.result.onError
import com.iti.common.result.onSuccess
import com.iti.core.datastore.models.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val editProfileRepo: EditProfileRepo,
    @param:Dispatcher(CareerPilotDispatchers.Default) private val dispatcherDefault: CoroutineDispatcher,
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
        viewModelScope.launch(dispatcherDefault) {
            val profile = editProfileRepo.userProfile.first()
            original = profile
            _state.update {
                it.copy(
                    displayName = profile.displayName,
                    username = profile.username,
                    email = profile.email,
                    gender = profile.gender,
                    dateOfBirth = profile.dateOfBirth,
                    dateOfBirthMillis = calculateMillis(profile.dateOfBirth),
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
                _state.update { 
                    it.copy(
                        displayName = action.value,
                        displayNameError = false
                    ) 
                }

            is EditProfileAction.OnUsernameChange ->
                _state.update {
                    it.copy(
                        username = action.value
                    )
                }

            is EditProfileAction.OnEmailChange ->
                _state.update {
                    it.copy(
                        email = action.value,
                        emailError = false
                    )
                }

            is EditProfileAction.OnGenderChange ->
                _state.update { it.copy(gender = action.value) }

            is EditProfileAction.OnDateOfBirthChange ->
                _state.update { 
                    it.copy(
                        dateOfBirth = action.value,
                        dateOfBirthMillis = calculateMillis(action.value)
                    ) 
                }

            is EditProfileAction.OnDateSelected -> {
                val date = Instant.ofEpochMilli(action.millis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                val formatted = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                _state.update { 
                    it.copy(
                        dateOfBirth = formatted,
                        dateOfBirthMillis = action.millis
                    ) 
                }
            }

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
                    viewModelScope.launch(dispatcherDefault) {
                        val startTime = System.currentTimeMillis()
                        var realProgress = 0
                        var isDone = false
                        var uploadResult: CareerPilotResult<FileUploadResponse, NetworkError>? = null

                        _state.update { 
                            it.copy(
                                isUploadingAvatar = true, 
                                avatarUploadProgress = 0,
                                uploadError = null
                            ) 
                        }

                        launch {
                            uploadResult = editProfileRepo.uploadImage(
                                uri = uri,
                                onProgress = { realProgress = it }
                            )
                            isDone = true
                        }

                        // Wait for upload to actually start or fail/finish
                        while (!isDone && realProgress == 0) {
                            delay(50.milliseconds)
                        }

                        var displayProgress = 0
                        while (true) {
                            if (isDone && uploadResult is CareerPilotResult.Error) break
                            
                            val target = if (isDone && uploadResult is CareerPilotResult.Success) 100 else realProgress
                            if (displayProgress < target) {
                                displayProgress++
                                _state.update { it.copy(avatarUploadProgress = displayProgress) }
                            }
                            
                            if (isDone && displayProgress >= 100) break
                            delay(20.milliseconds)
                        }

                        val elapsed = System.currentTimeMillis() - startTime
                        
                        uploadResult?.onSuccess { response ->
                            if (elapsed < 2000) delay((2000 - elapsed).milliseconds)
                            _state.update {
                                it.copy(
                                    avatarUrl = response.url,
                                    avatarLocalUri = uri.toString(),
                                    isUploadingAvatar = false,
                                    avatarUploadProgress = 0
                                )
                            }
                        }?.onError { error ->
                            _state.update { it.copy(uploadError = "Upload failed: ${error.name}") }
                            delay(2000.milliseconds)
                            _state.update {
                                it.copy(
                                    isUploadingAvatar = false,
                                    avatarUploadProgress = 0,
                                    uploadError = null
                                )
                            }
                        }
                    }
                }
            }

            is EditProfileAction.OnCVUpload -> {
                action.value?.let { uri ->
                    viewModelScope.launch(dispatcherDefault) {
                        val startTime = System.currentTimeMillis()
                        var realProgress = 0
                        var isDone = false
                        var uploadResult: CareerPilotResult<FileUploadResponse, NetworkError>? = null

                        _state.update { 
                            it.copy(
                                isUploadingCV = true, 
                                cvUploadProgress = 0,
                                uploadError = null
                            ) 
                        }

                        launch {
                            uploadResult = editProfileRepo.uploadCV(
                                uri = uri,
                                onProgress = { realProgress = it }
                            )
                            isDone = true
                        }

                        // Wait for upload to actually start or fail/finish
                        while (!isDone && realProgress == 0) {
                            delay(50.milliseconds)
                        }

                        var displayProgress = 0
                        while (true) {
                            if (isDone && uploadResult is CareerPilotResult.Error) break

                            val target = if (isDone && uploadResult is CareerPilotResult.Success) 100 else realProgress
                            if (displayProgress < target) {
                                displayProgress++
                                _state.update { it.copy(cvUploadProgress = displayProgress) }
                            }

                            if (isDone && displayProgress >= 100) break
                            delay(20.milliseconds)
                        }

                        val elapsed = System.currentTimeMillis() - startTime

                        uploadResult?.onSuccess { response ->
                            if (elapsed < 2000) delay((2000 - elapsed).milliseconds)
                            _state.update {
                                it.copy(
                                    isUploadingCV = false,
                                    cvUrl = response.url,
                                    cvFileName = response.originalName,
                                    cvFileSize = if (response.sizeBytes > 0) "${response.sizeBytes / 1024} KB" else "",
                                    cvUploadProgress = 0
                                )
                            }
                        }?.onError { error ->
                            _state.update { it.copy(uploadError = "Upload failed: ${error.name}") }
                            delay(2000.milliseconds)
                            _state.update {
                                it.copy(
                                    isUploadingCV = false,
                                    cvUploadProgress = 0,
                                    uploadError = null
                                )
                            }
                        }
                    }
                }
            }

            EditProfileAction.OnCVRemove -> {
                _state.update {
                    it.copy(
                        cvUrl = "",
                        cvFileName = "",
                        cvFileSize = "",
                        cvUploadDate = ""
                    )
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

            is EditProfileAction.OnSaveClick -> save(action.section)
        }
    }

    private fun save(section: ProfileEditSection) {
        val current = _state.value

        var hasError = false
        var displayNameErr = false
        var emailErr = false

        if (section == ProfileEditSection.ALL || section == ProfileEditSection.PERSONAL) {
            if (current.displayName.isBlank()) {
                displayNameErr = true
                hasError = true
            }
            if (current.email.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(current.email).matches()) {
                emailErr = true
                hasError = true
            }
        }

        if (hasError) {
            _state.update { 
                it.copy(
                    displayNameError = displayNameErr,
                    emailError = emailErr
                ) 
            }
            return
        }

        viewModelScope.launch(dispatcherDefault) {
            _state.update { 
                it.copy(
                    isLoading = true, 
                    displayNameError = false,
                    emailError = false
                ) 
            }
            val request = when (section) {
                ProfileEditSection.PERSONAL -> {
                    RequestProfileUpdate(
                        username = current.username.takeIf { it != original.username },
                        email = current.email.takeIf { it != original.email },
                        displayName = current.displayName.takeIf { it != original.displayName },
                        gender = current.gender.takeIf { it != original.gender },
                        dateOfBirth = current.dateOfBirth.takeIf { it != original.dateOfBirth },
                    )
                }
                ProfileEditSection.CAREER -> {
                    RequestProfileUpdate(
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
                }
                else -> {
                    RequestProfileUpdate(
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
                }
            }

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

    private fun calculateMillis(dateString: String): Long? {
        return runCatching {
            LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }.getOrNull()
    }

    private fun sendEvent(event: EditProfileEvent) {
        viewModelScope.launch(dispatcherDefault) { _events.send(event) }
    }
}