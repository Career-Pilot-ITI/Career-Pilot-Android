package com.iti.careerpilot.editprofile.presentation.state

import androidx.compose.runtime.Immutable

@Immutable
data class EditProfileState(
    val displayName: String = "",
    val username: String = "",
    val email: String = "",
    val gender: String = "",
    val dateOfBirth: String = "",
    val targetRole: String = "",
    val industry: String = "",
    val experienceLevel: String = "",
    val currentJobTitle: String = "",
    val yearsOfExperience: String = "",
    val skills: List<String> = emptyList(),
    val targetCompanies: List<String> = emptyList(),
    val educationLevel: String = "",
    val timezone: String = "",
    val avatarUrl: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isUploadingAvatar: Boolean = false,
    val avatarUploadProgress: Int = 0,
    val fieldErrors: Map<String, String> = emptyMap()
)

