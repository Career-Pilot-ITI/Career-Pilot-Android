package com.iti.careerpilot.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfileRequestDto(
    val username: String? = null,
    val email: String? = null,
    val displayName: String? = null,
    val avatarFileId: Long? = null,
    val gender: String? = null,
    val dateOfBirth: String? = null,
    val targetRole: String? = null,
    val industry: String? = null,
    val experienceLevel: String? = null,
    val currentJobTitle: String? = null,
    val yearsOfExperience: Int? = null,
    val cvFileId: Long? = null,
    val skills: List<String>? = null,
    val targetCompanies: List<String>? = null,
    val educationLevel: String? = null,
    val timezone: String? = null,
    val termsAccepted: Boolean? = null,
    val onboardingCompleted: Boolean? = null,
    val trackId: Long? = null,
)
