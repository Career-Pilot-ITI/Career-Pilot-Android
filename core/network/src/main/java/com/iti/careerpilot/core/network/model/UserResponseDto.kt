package com.iti.careerpilot.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class UserResponseDto(
    val id: Long,
    val phoneNumber: String? = null,
    val email: String? = null,
    val profile: UserProfileDto? = null,
    val newUser: Boolean = false
)

@Serializable
data class UserProfileDto(
    val displayName: String? = null,
    val username: String? = null,
    val email: String? = null,
    val targetRole: String? = null,
    val yearsOfExperience: Int? = null,
    val skills: List<String>? = null,
    val avatarUrl: String? = null,
    val currentJobTitle: String? = null,
    val industry: String? = null,
    val experienceLevel: String? = null,
    val educationLevel: String? = null,
    val targetCompanies: List<String>? = null,
    val timezone: String? = null
)
