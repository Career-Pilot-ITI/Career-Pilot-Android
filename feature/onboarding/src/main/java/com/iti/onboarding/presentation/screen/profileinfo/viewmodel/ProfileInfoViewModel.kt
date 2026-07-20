package com.iti.onboarding.presentation.screen.profileinfo.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.onboarding.R
import com.iti.onboarding.domain.usecase.UploadImageUseCase
import com.iti.onboarding.domain.usecase.SaveAvatarUrlUseCase
import com.iti.onboarding.domain.usecase.UpdateProfileUseCase
import com.iti.careerpilot.core.network.model.UpdateProfileRequestDto
import com.iti.common.media.ImageCaptureUriProvider
import com.iti.common.media.ImageSource
import com.iti.common.result.onSuccess
import com.iti.common.result.onError
import com.iti.common.util.toUIText
import com.iti.common.util.UIText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.iti.onboarding.domain.usecase.GetUserProfileUseCase

@HiltViewModel
class ProfileInfoViewModel @Inject constructor(
    private val imageCaptureUriProvider: ImageCaptureUriProvider,
    private val uploadImageUseCase: UploadImageUseCase,
    private val saveAvatarUrlUseCase: SaveAvatarUrlUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<ProfileInfoUiState>(ProfileInfoUiState())
    val state: StateFlow<ProfileInfoUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getUserProfileUseCase().collect { profile ->
                val url = profile.avatar.avatarUrl
                if (url.isNotBlank()) {
                    _state.update { it.updateData { data -> data.copy(avatarUrl = url, avatarFileId = profile.id.toLong()) } }
                }
            }
        }
    }

    private val _effect = MutableSharedFlow<ProfileInfoEffect>()
    val effect: SharedFlow<ProfileInfoEffect> = _effect.asSharedFlow()

    fun onIntent(intent: ProfileInfoIntent) {
        when (intent) {
            is ProfileInfoIntent.OnAvatarClicked ->
                _state.update { it.copy(isImageSourceSheetVisible = true) }

            is ProfileInfoIntent.OnImageSourceSelected -> {
                _state.update { it.copy(isImageSourceSheetVisible = false) }
                when (intent.source) {
                    ImageSource.CAMERA -> viewModelScope.launch {
                        val uri = imageCaptureUriProvider.createImageCaptureUri()
                        _effect.emit(ProfileInfoEffect.LaunchCamera(uri))
                    }
                    ImageSource.GALLERY -> Unit
                }
            }

            is ProfileInfoIntent.OnImagePicked -> {
                _state.update { it.updateData { data -> data.copy(selectedImageUri = intent.uri.toString()) }.copy(hasSuccessfullySubmitted = false) }
                uploadAvatar(intent.uri)
            }

            is ProfileInfoIntent.OnCameraPermissionDenied -> {
                _state.update { it.copy(isImageSourceSheetVisible = false) }
                viewModelScope.launch { _effect.emit(ProfileInfoEffect.OpenAppSettings) }
            }

            is ProfileInfoIntent.OnRetryPhotoUpload -> {
                _state.value.data.selectedImageUri?.let { uri ->
                    uploadAvatar(Uri.parse(uri))
                }
            }

            is ProfileInfoIntent.OnNameChanged -> _state.update { it.updateData { data -> data.copy(name = intent.name) }.copy(hasSuccessfullySubmitted = false) }
            is ProfileInfoIntent.OnEmailChanged -> _state.update { it.updateData { data -> data.copy(email = intent.email) }.copy(hasSuccessfullySubmitted = false) }
            is ProfileInfoIntent.OnTitleChanged -> _state.update { it.updateData { data -> data.copy(title = intent.title) }.copy(hasSuccessfullySubmitted = false) }
            is ProfileInfoIntent.OnExperienceChanged -> _state.update { it.updateData { data -> data.copy(experience = intent.experience) }.copy(hasSuccessfullySubmitted = false) }
            is ProfileInfoIntent.OnSkillsChanged -> _state.update { it.updateData { data -> data.copy(skills = intent.skills.toPersistentList()) }.copy(hasSuccessfullySubmitted = false) }
            is ProfileInfoIntent.OnSubmit -> {
                submitProfile()
            }
            is ProfileInfoIntent.OnDismissSheet -> {
                _state.update { it.copy(isImageSourceSheetVisible = false) }
            }
            is ProfileInfoIntent.OnOpenAppSettings ->
                viewModelScope.launch { _effect.emit(ProfileInfoEffect.OpenAppSettings) }
            is ProfileInfoIntent.OnShowAddSkillDialogChanged -> {
                _state.update { it.copy(showAddSkillDialog = intent.show) }
            }
            is ProfileInfoIntent.OnNewSkillTextChanged -> {
                _state.update { it.copy(newSkillText = intent.text) }
            }
            is ProfileInfoIntent.OnConfirmAddSkill -> {
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
            is ProfileInfoIntent.OnInitDefaultSkills -> {
                _state.update { currentState ->
                    if (currentState.allSkills.isEmpty()) {
                        currentState.copy(allSkills = intent.defaultSkills.toPersistentList())
                    } else currentState
                }
            }
        }
    }

    private fun uploadAvatar(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isImageUploading = true) }
            uploadImageUseCase(uri)
                .onSuccess { uploaded ->
                    saveAvatarUrlUseCase(uploaded)
                    _state.update { it.updateData { data -> data.copy(avatarUrl = uploaded.url, avatarFileId = uploaded.id) } }
                }
                .onError { error ->
                    _effect.emit(
                        ProfileInfoEffect.ShowSnackbar(
                            messageRes = (error.toUIText() as UIText.StringResource).resId,
                            actionLabelRes = R.string.profile_info_photo_retry,
                            actionIntent = ProfileInfoIntent.OnRetryPhotoUpload
                        )
                    )
                }
            _state.update { it.copy(isImageUploading = false) }
        }
    }

    private fun submitProfile() {
        viewModelScope.launch {
            if (_state.value.hasSuccessfullySubmitted) {
                _effect.emit(ProfileInfoEffect.NavigateToNextScreen)
                return@launch
            }
            _state.update { it.copy(isSubmitting = true) }
            
            val currentData = _state.value.data
            val yearsOfExperience = currentData.experience.toIntOrNull()
            
            val request = UpdateProfileRequestDto(
                displayName = currentData.name.takeIf { it.isNotBlank() },
                email = currentData.email.takeIf { it.isNotBlank() },
                targetRole = currentData.title.takeIf { it.isNotBlank() },
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
                    _effect.emit(
                        ProfileInfoEffect.ShowSnackbar(
                            messageRes = (error.toUIText() as UIText.StringResource).resId
                        )
                    )
                }
                
            _state.update { it.copy(isSubmitting = false) }
        }
    }
}
