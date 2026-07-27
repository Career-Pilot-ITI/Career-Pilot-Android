package com.iti.careerpilot.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class UserResponseDto(
    val id: Long,
    val phoneNumber: String? = null,
    val profile: UserProfileDto? = null,
    val newUser: Boolean = false,
)

@Serializable
data class UserProfileDto(
    val id: Long? = null,
    val phoneNumber: String? = null,
    val displayName: String? = null,
    val username: String? = null,
    val email: String? = null,
    val avatarUrl: String? = null,
    val gender: String? = null,
    val dateOfBirth: String? = null,
    val targetRole: String? = null,
    val industry: String? = null,
    val experienceLevel: String? = null,
    val currentJobTitle: String? = null,
    val yearsOfExperience: Int? = null,
    val cvUrl: String? = null,
    val skills: List<SkillDto>? = null,
    val targetCompanies: List<String>? = null,
    val educationLevel: String? = null,
    val timezone: String? = null,
    val termsAccepted: Boolean? = null,
    val subscriptionTier: String? = null,
    val coinBalance: Int? = null,
    val onboardingCompleted: Boolean? = null,
    val trackName: String? = null,
    val trackId: Long? = null,
)

@Serializable
data class SkillDto(
    val skillName: String? = null,
    val category: String? = null,
    val performanceScore: Int? = null,
    val timesAssessed: Int? = null,
    val lastAssessedAt: String? = null,
)
