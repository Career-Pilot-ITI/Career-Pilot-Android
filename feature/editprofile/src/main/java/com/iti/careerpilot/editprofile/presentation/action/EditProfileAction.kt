package com.iti.careerpilot.editprofile.presentation.action

import android.net.Uri
import com.iti.common.model.ProfileEditSection


sealed interface EditProfileAction {
    data class OnDisplayNameChange(val value: String) : EditProfileAction
    data class OnUsernameChange(val value: String) : EditProfileAction
    data class OnEmailChange(val value: String) : EditProfileAction
    data class OnGenderChange(val value: String) : EditProfileAction
    data class OnDateOfBirthChange(val value: String) : EditProfileAction
    data class OnTargetRoleChange(val value: String) : EditProfileAction
    data class OnIndustryChange(val value: String) : EditProfileAction
    data class OnExperienceLevelChange(val value: String) : EditProfileAction
    data class OnTrackChange(val value: String) : EditProfileAction
    data class OnCurrentJobTitleChange(val value: String) : EditProfileAction
    data class OnYearsOfExperienceChange(val value: String) : EditProfileAction
    data class OnEducationLevelChange(val value: String) : EditProfileAction
    data class OnTimezoneChange(val value: String) : EditProfileAction
    data class OnAvatarChange(val value: Uri?) : EditProfileAction
    data class OnCVUpload(val value: Uri?) : EditProfileAction
    data object OnCVRemove : EditProfileAction

    data class OnSkillAdd(val skill: String) : EditProfileAction
    data class OnSkillRemove(val skill: String) : EditProfileAction
    data class OnTargetCompanyAdd(val company: String) : EditProfileAction
    data class OnTargetCompanyRemove(val company: String) : EditProfileAction

    data class OnSaveClick(val section: ProfileEditSection = ProfileEditSection.ALL) : EditProfileAction
    data object OnBackClick : EditProfileAction
}