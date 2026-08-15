package com.iti.onboarding.presentation.screen.profileinfo.viewmodel

import android.net.Uri
import androidx.annotation.StringRes
import com.iti.common.media.ImageSource
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class ProfileInfoData(
    val name: String = "",
    val email: String = "",
    val title: String = "",
    val experience: String = "",
    val skills: ImmutableList<String> = persistentListOf(),
    val avatarUrl: String? = null,
    val selectedImageUri: String? = null,
    val avatarFileId: Long? = null,
)

data class ProfileInfoUiState(
    val data: ProfileInfoData = ProfileInfoData(),
    val isImageSourceSheetVisible: Boolean = false,
    val isImageUploading: Boolean = false,
    val uploadProgress: Int = 0,
    val isSubmitting: Boolean = false,
    val hasSuccessfullySubmitted: Boolean = false,
    val allSkills: ImmutableList<String> = persistentListOf(),
    val showAddSkillDialog: Boolean = false,
    val newSkillText: String = "",
    val isEmailInvalid: Boolean = false,
) {
    val isFormValid: Boolean
        get() {
            return data.name.isNotBlank() && data.email.isNotBlank() && data.title.isNotBlank() && data.experience.isNotBlank()
        }

    val isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(data.email).matches()
}

fun ProfileInfoUiState.updateData(
    transform: (ProfileInfoData) -> ProfileInfoData
): ProfileInfoUiState = copy(data = transform(data))

sealed interface ProfileInfoIntent {
    data object Initial : ProfileInfoIntent
    data object OnAvatarClicked : ProfileInfoIntent
    data class OnImageSourceSelected(val source: ImageSource) : ProfileInfoIntent
    data class OnImagePicked(val uri: Uri) : ProfileInfoIntent
    data object OnCameraPermissionDenied : ProfileInfoIntent
    data object OnRetryPhotoUpload : ProfileInfoIntent
    data class OnNameChanged(val name: String) : ProfileInfoIntent
    data class OnEmailChanged(val email: String) : ProfileInfoIntent
    data class OnTitleChanged(val title: String) : ProfileInfoIntent
    data class OnExperienceChanged(val experience: String) : ProfileInfoIntent
    data class OnSkillsChanged(val skills: List<String>) : ProfileInfoIntent
    data object OnSubmit : ProfileInfoIntent
    data object OnDismissSheet : ProfileInfoIntent
    data object OnOpenAppSettings : ProfileInfoIntent
    data class OnShowAddSkillDialogChanged(val show: Boolean) : ProfileInfoIntent
    data class OnNewSkillTextChanged(val text: String) : ProfileInfoIntent
    data object OnConfirmAddSkill : ProfileInfoIntent
    data class OnInitDefaultSkills(val defaultSkills: List<String>) : ProfileInfoIntent
}

sealed interface ProfileInfoEffect {
    data class LaunchCamera(val captureUri: Uri) : ProfileInfoEffect
    data object RequestCameraPermission : ProfileInfoEffect
    data object OpenAppSettings : ProfileInfoEffect
    data object NavigateToNextScreen : ProfileInfoEffect
}
