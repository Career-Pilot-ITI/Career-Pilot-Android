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
    val trackName: String = "",  //todo add in ui
    val currentJobTitle: String = "",
    val yearsOfExperience: String = "",
    val skills: List<String> = emptyList(),
    val targetCompanies: List<String> = emptyList(),
    val educationLevel: String = "",
    val timezone: String = "",
    val avatarUrl: String = "",
    val avatarLocalUri: String = "",
    val cvUrl: String = "",
    val cvFileName: String = "",
    val cvFileSize: String = "",
    val cvUploadDate: String = "",
    val isLoading: Boolean = false,
    val isUploadingAvatar: Boolean = false,
    val isUploadingCV: Boolean = false,
    val avatarUploadProgress: Int = 0,
    val cvUploadProgress: Int = 0,
    val fieldErrors: Map<String, String> = emptyMap()
)

