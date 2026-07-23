package com.iti.careerpilot.editprofile.presentation.viewmodel

import android.net.Uri
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.editprofile.data.datasource.remote.models.FileUploadResponse
import com.iti.careerpilot.editprofile.domain.models.RequestProfileUpdate
import com.iti.careerpilot.editprofile.domain.repo.EditProfileRepo
import com.iti.careerpilot.editprofile.presentation.action.EditProfileAction
import com.iti.careerpilot.editprofile.presentation.event.EditProfileEvent
import com.iti.careerpilot.editprofile.presentation.state.EditProfileState
import com.iti.common.dispatcher.CareerPilotDispatchers
import com.iti.common.dispatcher.Dispatcher
import com.iti.common.error.NetworkError
import com.iti.common.model.ProfileEditSection
import com.iti.common.result.CareerPilotResult
import com.iti.common.result.onError
import com.iti.common.result.onSuccess
import com.iti.common.snackbar.CareerPilotSnackbarController
import com.iti.common.util.toUIText
import com.iti.core.datastore.models.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
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
        viewModelScope.launch {
            val profile = editProfileRepo.userProfile.value
            original = profile
            val birthMillis = calculateMillis(profile.personal.dateOfBirth)
            _state.update {
                it.copy(
                    displayName = profile.personal.displayName,
                    username = profile.account.username,
                    email = profile.account.email,
                    gender = profile.personal.gender,
                    dateOfBirth = profile.personal.dateOfBirth,
                    dateOfBirthMillis = birthMillis,
                    dateOfBirthDisplay = formatLocalizedDate(birthMillis),
                    targetRole = profile.career.targetRole,
                    industry = profile.career.industry,
                    experienceLevel = profile.career.experienceLevel,
                    trackName = profile.career.trackName,
                    currentJobTitle = profile.career.currentJobTitle,
                    yearsOfExperience = profile.career.yearsOfExperience.toString(),
                    skills = profile.career.skills,
                    targetCompanies = profile.career.targetCompanies,
                    educationLevel = profile.career.educationLevel,
                    timezone = profile.account.timezone,
                    avatarUrl = profile.avatar.avatarUrl,
                    avatarLocalUri = profile.avatar.avatarLocalUri,
                    cvUrl = profile.cv.cvUrl,
                    cvLocalUri = profile.cv.cvLocalUri,
                    cvFileName = profile.cv.cvFileName,
                    cvFileSize = if (profile.cv.cvSizeBytes > 0) "${profile.cv.cvSizeBytes / 1024} KB" else "",
                    isLoading = false
                )
            }
        }
    }

    fun onAction(action: EditProfileAction) {
        when (action) {
            is EditProfileAction.OnDisplayNameChange ->
                _state.update { it.copy(displayName = action.value, displayNameError = false) }

            is EditProfileAction.OnUsernameChange ->
                _state.update { it.copy(username = action.value) }

            is EditProfileAction.OnEmailChange ->
                _state.update { it.copy(email = action.value, emailError = false) }

            is EditProfileAction.OnGenderChange ->
                _state.update { it.copy(gender = action.value) }

            is EditProfileAction.OnDateOfBirthChange -> {
                val millis = calculateMillis(action.value)
                _state.update {
                    it.copy(
                        dateOfBirth = action.value,
                        dateOfBirthMillis = millis,
                        dateOfBirthDisplay = formatLocalizedDate(millis)
                    )
                }
            }

            is EditProfileAction.OnDateSelected -> {
                _state.update {
                    it.copy(
                        dateOfBirth = formatBackendDate(action.millis),
                        dateOfBirthMillis = action.millis,
                        dateOfBirthDisplay = formatLocalizedDate(action.millis)
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

            is EditProfileAction.OnAvatarChange -> uploadAvatar(action.value)

            is EditProfileAction.OnCVUpload -> uploadCV(action.value)

            EditProfileAction.OnCVRemove ->
                _state.update {
                    it.copy(
                        cvUrl = "",
                        cvLocalUri = "",
                        cvFileId = null,
                        cvFileName = "",
                        cvFileSize = "",
                        cvUploadDate = ""
                    )
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

    private fun uploadAvatar(uri: Uri?) {
        uri?.let { newUri ->
            performFileUpload(
                uri = newUri,
                uploadCall = { u, p -> editProfileRepo.uploadImage(u, p) },
                onStart = {
                    _state.update { s ->
                        s.copy(isUploadingAvatar = true, avatarUploadProgress = 0)
                    }
                },
                onProgress = { p -> _state.update { it.copy(avatarUploadProgress = p) } },
                onSuccess = { resp ->
                    _state.update {
                        it.copy(
                            avatarUrl = resp.url,
                            avatarLocalUri = uri.toString(),
                            avatarFileId = resp.id,
                        )
                    }
                },
                onFinish = {
                    _state.update { it.copy(isUploadingAvatar = false, avatarUploadProgress = 0) }
                }
            )
        }
    }

    private fun uploadCV(uri: Uri?) {
        uri?.let { newUri ->
            performFileUpload(
                uri = newUri,
                uploadCall = { u, p -> editProfileRepo.uploadCV(u, p) },
                onStart = {
                    _state.update { s ->
                        s.copy(isUploadingCV = true, cvUploadProgress = 0)
                    }
                },
                onProgress = { p -> _state.update { it.copy(cvUploadProgress = p) } },
                onSuccess = { resp ->
                    _state.update {
                        it.copy(
                            cvUrl = resp.url,
                            cvLocalUri = uri.toString(),
                            cvFileId = resp.id,
                            cvFileName = resp.originalName,
                            cvFileSize = if (resp.sizeBytes > 0) "${resp.sizeBytes / (1024f * 1024f)} MB" else ""
                        )
                    }
                },
                onFinish = {
                    _state.update { it.copy(isUploadingCV = false, cvUploadProgress = 0) }
                }
            )
        }
    }

    private fun performFileUpload(
        uri: Uri,
        uploadCall: suspend (Uri, (Int) -> Unit) -> CareerPilotResult<FileUploadResponse, NetworkError>,
        onStart: () -> Unit,
        onProgress: (Int) -> Unit,
        onSuccess: suspend (FileUploadResponse) -> Unit,
        onFinish: () -> Unit
    ) {
        viewModelScope.launch(dispatcherDefault) {
            val startTime = System.currentTimeMillis()
            var realProgress = 0
            var isDone = false
            var uploadResult: CareerPilotResult<FileUploadResponse, NetworkError>? = null

            onStart()

            launch {
                uploadResult = uploadCall(uri) { realProgress = it }
                isDone = true
            }

            while (!isDone && realProgress == 0) delay(50.milliseconds)

            var displayProgress = 0
            while (true) {
                if (isDone && uploadResult is CareerPilotResult.Error) break
                val target = if (isDone && uploadResult is CareerPilotResult.Success) 100 else realProgress
                if (displayProgress < target) {
                    displayProgress++
                    onProgress(displayProgress)
                }
                if (isDone && displayProgress >= 100) break
                delay(20.milliseconds)
            }

            val elapsed = System.currentTimeMillis() - startTime
            uploadResult?.onSuccess { response ->
                if (elapsed < 2000) delay((2000 - elapsed).milliseconds)
                onSuccess(response)
            }?.onError { error ->
                CareerPilotSnackbarController.show(error.toUIText())
            }
            onFinish()
        }
    }

    private fun save(section: ProfileEditSection) {
        if (!validateInputs(section)) return

        viewModelScope.launch(dispatcherDefault) {
            _state.update { it.copy(isLoading = true, displayNameError = false, emailError = false) }
            val request = createUpdateRequest(section)

            editProfileRepo.updateProfile(request)
                .onSuccess {
                    _state.update { it.copy(isLoading = false) }
                    _events.send(EditProfileEvent.NavigateBack)
                }
                .onError {
                    _state.update { it.copy(isLoading = false) }
                    CareerPilotSnackbarController.show(it.toUIText())
                }
        }
    }

    private fun validateInputs(section: ProfileEditSection): Boolean {
        val current = _state.value
        var displayNameErr = false
        var emailErr = false

        if (section == ProfileEditSection.ALL || section == ProfileEditSection.PERSONAL) {
            if (current.displayName.isBlank()) displayNameErr = true
            if (current.email.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(current.email).matches()) emailErr = true
        }

        val hasError = displayNameErr || emailErr
        if (hasError) {
            _state.update { it.copy(displayNameError = displayNameErr, emailError = emailErr) }
        }
        return !hasError
    }

    private fun createUpdateRequest(section: ProfileEditSection): RequestProfileUpdate {
        val current = _state.value
        return when (section) {
            ProfileEditSection.PERSONAL -> RequestProfileUpdate(
                username = current.username.takeIf { it != original.account.username },
                email = current.email.takeIf { it != original.account.email },
                displayName = current.displayName.takeIf { it != original.personal.displayName },
                gender = current.gender.takeIf { it != original.personal.gender },
                dateOfBirth = current.dateOfBirth.takeIf { it != original.personal.dateOfBirth },
                avatarFileId = current.avatarFileId,
            )

            ProfileEditSection.CAREER -> RequestProfileUpdate(
                targetRole = current.targetRole.takeIf { it != original.career.targetRole },
                industry = current.industry.takeIf { it != original.career.industry },
                experienceLevel = current.experienceLevel.takeIf { it != original.career.experienceLevel },
                currentJobTitle = current.currentJobTitle.takeIf { it != original.career.currentJobTitle },
                yearsOfExperience = current.yearsOfExperience.toIntOrNull()
                    ?.takeIf { it != original.career.yearsOfExperience },
                cvFileId = current.cvFileId,
                skills = current.skills.takeIf { it != original.career.skills },
                targetCompanies = current.targetCompanies.takeIf { it != original.career.targetCompanies },
                educationLevel = current.educationLevel.takeIf { it != original.career.educationLevel },
                timezone = current.timezone.takeIf { it != original.account.timezone },
            )

            else -> RequestProfileUpdate(
                username = current.username.takeIf { it != original.account.username },
                email = current.email.takeIf { it != original.account.email },
                displayName = current.displayName.takeIf { it != original.personal.displayName },
                gender = current.gender.takeIf { it != original.personal.gender },
                dateOfBirth = current.dateOfBirth.takeIf { it != original.personal.dateOfBirth },
                avatarFileId = current.avatarFileId,
                targetRole = current.targetRole.takeIf { it != original.career.targetRole },
                industry = current.industry.takeIf { it != original.career.industry },
                experienceLevel = current.experienceLevel.takeIf { it != original.career.experienceLevel },
                currentJobTitle = current.currentJobTitle.takeIf { it != original.career.currentJobTitle },
                yearsOfExperience = current.yearsOfExperience.toIntOrNull()
                    ?.takeIf { it != original.career.yearsOfExperience },
                cvFileId = current.cvFileId,
                skills = current.skills.takeIf { it != original.career.skills },
                targetCompanies = current.targetCompanies.takeIf { it != original.career.targetCompanies },
                educationLevel = current.educationLevel.takeIf { it != original.career.educationLevel },
                timezone = current.timezone.takeIf { it != original.account.timezone },
            )
        }
    }

    private fun calculateMillis(dateString: String): Long? {
        return runCatching {
            LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
        }.getOrNull()
    }

    private fun formatLocalizedDate(millis: Long?): String {
        return millis?.let {
            Instant.ofEpochMilli(it)
                .atZone(ZoneOffset.UTC)
                .toLocalDate()
                .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
        } ?: ""
    }

    private fun formatBackendDate(millis: Long?): String {
        return millis?.let {
            Instant.ofEpochMilli(it)
                .atZone(ZoneOffset.UTC)
                .toLocalDate()
                .format(DateTimeFormatter.ISO_LOCAL_DATE)
        } ?: ""
    }

    private fun sendEvent(event: EditProfileEvent) {
        viewModelScope.launch(dispatcherDefault) { _events.send(event) }
    }
}
