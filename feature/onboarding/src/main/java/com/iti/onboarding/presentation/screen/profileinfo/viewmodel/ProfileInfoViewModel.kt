package com.iti.onboarding.presentation.screen.profileinfo.viewmodel

import android.net.Uri
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.core.network.model.UpdateProfileRequestDto
import com.iti.common.dispatcher.CareerPilotDispatchers
import com.iti.common.dispatcher.Dispatcher
import com.iti.common.error.NetworkError
import com.iti.common.media.ImageCaptureUriProvider
import com.iti.common.media.ImageSource
import com.iti.common.result.CareerPilotResult
import com.iti.common.result.onError
import com.iti.common.result.onSuccess
import com.iti.common.snackbar.CareerPilotSnackbarController
import com.iti.common.util.UIText
import com.iti.common.util.toUIText
import com.iti.onboarding.R
import com.iti.onboarding.domain.model.UploadedFile
import com.iti.onboarding.domain.usecase.GetUserProfileUseCase
import com.iti.onboarding.domain.usecase.SaveAvatarUrlUseCase
import com.iti.onboarding.domain.usecase.UpdateProfileUseCase
import com.iti.onboarding.domain.usecase.UploadImageUseCase
import com.iti.onboarding.presentation.screen.profileinfo.model.ExperienceLevel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds



@HiltViewModel
class ProfileInfoViewModel @Inject constructor(
    private val imageCaptureUriProvider: ImageCaptureUriProvider,
    private val uploadImageUseCase: UploadImageUseCase,
    private val saveAvatarUrlUseCase: SaveAvatarUrlUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    @param:Dispatcher(CareerPilotDispatchers.Default) private val dispatcherDefault: CoroutineDispatcher,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileInfoUiState())
    val state: StateFlow<ProfileInfoUiState> = _state.asStateFlow()
    private var profileObservationJob: Job? = null

    private fun observeProfile() {
        if (profileObservationJob != null) return
        profileObservationJob = viewModelScope.launch {
            getUserProfileUseCase().collect { profile ->
                _state.update { currentState ->
                    val cvSkills = profile.career.skills
                    val mergedSkills = if (cvSkills.isNotEmpty()) {
                        (currentState.data.skills + cvSkills).distinct().toPersistentList()
                    } else {
                        currentState.data.skills
                    }

                    val mergedAllSkills = if (cvSkills.isNotEmpty()) {
                        (currentState.allSkills + cvSkills).distinct().toPersistentList()
                    } else {
                        currentState.allSkills
                    }

                    val targetTitle = profile.career.targetRole.ifBlank { profile.career.currentJobTitle }
                    val experienceText = when {
                        profile.career.experienceLevel.isNotBlank() -> profile.career.experienceLevel
                        profile.career.yearsOfExperience > 0 -> ExperienceLevel.fromYears(profile.career.yearsOfExperience).apiKey
                        else -> currentState.data.experience
                    }

                    currentState.copy(
                        allSkills = mergedAllSkills,
                        data = currentState.data.copy(
                            avatarUrl = profile.avatar.avatarUrl.takeIf { it.isNotBlank() } ?: currentState.data.avatarUrl,
                            name = profile.personal.displayName.ifBlank { currentState.data.name },
                            email = profile.account.email.ifBlank { currentState.data.email },
                            title = targetTitle.ifBlank { currentState.data.title },
                            experience = experienceText,
                            skills = mergedSkills
                        )
                    )
                }
            }
        }
    }

    private val _effect = MutableSharedFlow<ProfileInfoEffect>()
    val effect: SharedFlow<ProfileInfoEffect> = _effect.asSharedFlow()

    fun onIntent(intent: ProfileInfoIntent) {
        when (intent) {
            ProfileInfoIntent.Initial -> observeProfile()
            is ProfileInfoIntent.OnAvatarClicked -> setImageSourceSheetVisible(true)

            is ProfileInfoIntent.OnImageSourceSelected -> handleImageSourceSelected(intent.source)

            is ProfileInfoIntent.OnImagePicked -> handleImagePicked(intent.uri)

            is ProfileInfoIntent.OnCameraPermissionDenied -> handleCameraPermissionDenied()

            is ProfileInfoIntent.OnRetryPhotoUpload -> retryPhotoUpload()

            is ProfileInfoIntent.OnNameChanged ->
                updateProfile {
                    updateData { it.copy(name = intent.name) }
                }

            is ProfileInfoIntent.OnEmailChanged ->
                updateProfile {
                    updateData { it.copy(email = intent.email) }.copy(isEmailInvalid = false)
                }

            is ProfileInfoIntent.OnTitleChanged ->
                updateProfile {
                    updateData { it.copy(title = intent.title) }
                }

            is ProfileInfoIntent.OnExperienceChanged ->
                updateProfile {
                    updateData { it.copy(experience = intent.experience) }
                }

            is ProfileInfoIntent.OnSkillsChanged ->
                updateProfile {
                    updateData {
                        it.copy(skills = intent.skills.toPersistentList())
                    }
                }

            is ProfileInfoIntent.OnSubmit -> submitProfile()

            is ProfileInfoIntent.OnDismissSheet -> setImageSourceSheetVisible(false)

            is ProfileInfoIntent.OnOpenAppSettings -> emitEffect(ProfileInfoEffect.OpenAppSettings)

            is ProfileInfoIntent.OnShowAddSkillDialogChanged -> setAddSkillDialogVisible(intent.show)

            is ProfileInfoIntent.OnNewSkillTextChanged -> updateNewSkillText(intent.text)

            is ProfileInfoIntent.OnConfirmAddSkill -> confirmAddSkill()

            is ProfileInfoIntent.OnInitDefaultSkills -> initializeDefaultSkills(intent.defaultSkills)
        }
    }

    private inline fun updateProfile(
        crossinline transform: ProfileInfoUiState.() -> ProfileInfoUiState,
    ) {
        _state.update { currentState ->
            currentState
                .transform()
                .copy(hasSuccessfullySubmitted = false)
        }
    }

    private fun setImageSourceSheetVisible(isVisible: Boolean) {
        _state.update {
            it.copy(isImageSourceSheetVisible = isVisible)
        }
    }

    private fun setAddSkillDialogVisible(isVisible: Boolean) {
        _state.update {
            it.copy(showAddSkillDialog = isVisible)
        }
    }

    private fun updateNewSkillText(text: String) {
        _state.update {
            it.copy(newSkillText = text)
        }
    }

    private fun emitEffect(effect: ProfileInfoEffect) {
        viewModelScope.launch {
            _effect.emit(effect)
        }
    }

    private fun handleImageSourceSelected(source: ImageSource) {
        setImageSourceSheetVisible(false)

        when (source) {
            ImageSource.CAMERA -> launchCamera()
            ImageSource.GALLERY -> Unit
        }
    }

    private fun launchCamera() {
        val uri = imageCaptureUriProvider.createImageCaptureUri()

        emitEffect(
            ProfileInfoEffect.LaunchCamera(uri),
        )
    }

    private fun handleImagePicked(uri: Uri) {
        updateProfile {
            updateData {
                it.copy(selectedImageUri = uri.toString())
            }
        }
        uploadAvatar(uri)
    }

    private fun handleCameraPermissionDenied() {
        viewModelScope.launch {
            CareerPilotSnackbarController.show(
                UIText.StringResource(R.string.profile_info_camera_permission_message)
            )
        }
    }

    private fun retryPhotoUpload() {
        val selectedUri = _state.value.data.selectedImageUri
        if (!selectedUri.isNullOrBlank()) {
            uploadAvatar(Uri.parse(selectedUri))
        }
    }

    private fun confirmAddSkill() {
        val skill = _state.value.newSkillText.trim()
        if (skill.isNotEmpty()) {
            _state.update { currentState ->
                val newAllSkills = if (!currentState.allSkills.contains(skill)) {
                    (currentState.allSkills + skill).toPersistentList()
                } else currentState.allSkills

                val newDataSkills = if (!currentState.data.skills.contains(skill)) {
                    (currentState.data.skills + skill).toPersistentList()
                } else currentState.data.skills

                currentState.copy(
                    allSkills = newAllSkills,
                    data = currentState.data.copy(skills = newDataSkills),
                    newSkillText = "",
                    showAddSkillDialog = false,
                    hasSuccessfullySubmitted = false
                )
            }
        } else {
            _state.update { it.copy(newSkillText = "", showAddSkillDialog = false) }
        }
    }

    private fun initializeDefaultSkills(defaultSkills: List<String>) {
        _state.update { currentState ->
            if (currentState.allSkills.isEmpty()) {
                currentState.copy(allSkills = defaultSkills.toPersistentList())
            } else currentState
        }
    }

    private fun uploadAvatar(uri: Uri) {
        performFileUpload(
            uri = uri,
            uploadCall = { u, p -> uploadImageUseCase(u, p) },
            onStart = {
                _state.update { s ->
                    s.copy(isImageUploading = true, uploadProgress = 0)
                }
            },
            onProgress = { p -> _state.update { it.copy(uploadProgress = p) } },
            onSuccess = { uploaded ->
                saveAvatarUrlUseCase(uploaded)
                _state.update {
                    it.updateData { data ->
                        data.copy(
                            avatarUrl = uploaded.url,
                            avatarFileId = uploaded.id
                        )
                    }
                }
            },
            onFinish = {
                _state.update { it.copy(isImageUploading = false, uploadProgress = 0) }
            }
        )
    }

    private fun performFileUpload(
        uri: Uri,
        uploadCall: suspend (Uri, (Int) -> Unit) -> CareerPilotResult<UploadedFile, NetworkError>,
        onStart: () -> Unit,
        onProgress: (Int) -> Unit,
        onSuccess: suspend (UploadedFile) -> Unit,
        onFinish: () -> Unit
    ) {
        viewModelScope.launch(dispatcherDefault) {
            val startTime = System.currentTimeMillis()
            var realProgress = 0
            var isDone = false
            var uploadResult: CareerPilotResult<UploadedFile, NetworkError>? = null

            onStart()

            launch {
                uploadResult = uploadCall(uri) { realProgress = it }
                isDone = true
            }

            while (!isDone && realProgress == 0) delay(50.milliseconds)

            var displayProgress = 0
            while (true) {
                if (isDone && uploadResult is CareerPilotResult.Error) break
                val target =
                    if (isDone && uploadResult is CareerPilotResult.Success) 100 else realProgress
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
                viewModelScope.launch {
                    CareerPilotSnackbarController.show(error.toUIText())
                }
            }
            onFinish()
        }
    }

    private fun submitProfile() {
        viewModelScope.launch {
            if (_state.value.hasSuccessfullySubmitted) {
                _effect.emit(ProfileInfoEffect.NavigateToNextScreen)
                return@launch
            }
            val currentData = _state.value.data
            val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(currentData.email).matches()
            if (!isEmailValid) {
                _state.update { it.copy(isEmailInvalid = true) }
                CareerPilotSnackbarController.show(UIText.StringResource(R.string.profile_info_email_invalid))
                return@launch
            }

            _state.update { it.copy(isSubmitting = true, isEmailInvalid = false) }

            val selectedLevel = ExperienceLevel.fromString(currentData.experience)
            val experienceLevel = selectedLevel?.apiKey ?: currentData.experience.takeIf { it.isNotBlank() }
            val yearsOfExperience = currentData.experience.toIntOrNull() ?: selectedLevel?.defaultYears

            val request = UpdateProfileRequestDto(
                displayName = currentData.name.takeIf { it.isNotBlank() },
                email = currentData.email.takeIf { it.isNotBlank() },
                targetRole = currentData.title.takeIf { it.isNotBlank() },
                experienceLevel = experienceLevel,
                yearsOfExperience = yearsOfExperience,
                skills = currentData.skills.takeIf { it.isNotEmpty() },
                avatarFileId = currentData.avatarFileId
            )

            updateProfileUseCase(request)
                .onSuccess {
                    _state.update { it.copy(hasSuccessfullySubmitted = true) }
                    _effect.emit(ProfileInfoEffect.NavigateToNextScreen)
                }
                .onError { error ->
                    if (error == NetworkError.CONFLICT) {
                        _state.update { it.copy(isEmailInvalid = true) }
                    }
                    viewModelScope.launch {
                        CareerPilotSnackbarController.show(error.toUIText())
                    }
                }

            _state.update { it.copy(isSubmitting = false) }
        }
    }
}
