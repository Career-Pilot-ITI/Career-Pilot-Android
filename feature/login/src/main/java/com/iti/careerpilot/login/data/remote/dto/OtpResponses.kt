package com.iti.careerpilot.login.data.remote.dto

import com.iti.careerpilot.core.network.model.AuthTokensDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiMessageResponse(
    @SerialName("message") val message: String? = null,
    @SerialName("success") val success: Boolean = false,
)

@Serializable
data class OtpAuthResponse(
    @SerialName("authTokens") val authTokens: AuthTokensDto,
    @SerialName("user") val user: AuthUserDto,
)

@Serializable
data class AuthUserDto(
    @SerialName("id") val id: Long,
    @SerialName("phoneNumber") val phoneNumber: String? = null,
    @SerialName("profile") val profile: AuthUserProfileDto? = null,
    @SerialName("newUser") val newUser: Boolean = false,
)

@Serializable
data class AuthUserProfileDto(
    @SerialName("username") val username: String? = null,
    @SerialName("displayName") val displayName: String? = null,
    @SerialName("email") val email: String? = null,
    @SerialName("avatarUrl") val avatarUrl: String? = null,
    @SerialName("gender") val gender: String? = null,
    @SerialName("dateOfBirth") val dateOfBirth: String? = null,
    @SerialName("targetRole") val targetRole: String? = null,
    @SerialName("industry") val industry: String? = null,
    @SerialName("experienceLevel") val experienceLevel: String? = null,
    @SerialName("currentJobTitle") val currentJobTitle: String? = null,
    @SerialName("yearsOfExperience") val yearsOfExperience: Int? = null,
    @SerialName("cvUrl") val cvUrl: String? = null,
    @SerialName("skills") val skills: List<AuthSkillDto>? = null,
    @SerialName("targetCompanies") val targetCompanies: String? = null,
    @SerialName("educationLevel") val educationLevel: String? = null,
    @SerialName("timezone") val timezone: String? = null,
    @SerialName("termsAccepted") val termsAccepted: Boolean? = null,
    @SerialName("onboardingCompleted") val onboardingCompleted: Boolean = false,
    @SerialName("trackName") val trackName: String? = null,
    @SerialName("subscriptionTier") val subscriptionTier: String? = null,
    @SerialName("coinBalance") val coinBalance: Int? = null,
)

@Serializable
data class AuthSkillDto(
    @SerialName("skillName") val skillName: String? = null,
    @SerialName("category") val category: String? = null,
    @SerialName("performanceScore") val performanceScore: Int? = null,
    @SerialName("timesAssessed") val timesAssessed: Int? = null,
    @SerialName("lastAssessedAt") val lastAssessedAt: String? = null,
)
