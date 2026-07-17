package com.iti.careerpilot.login.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ApiMessageResponse(
    val message: String? = null,
    val success: Boolean = false,
)

@Serializable
data class OtpAuthResponse(
    val authTokens: AuthTokensDto,
    val user: AuthUserDto,
)

@Serializable
data class AuthTokensDto(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long = 0L,
)

@Serializable
data class AuthUserDto(
    val id: Long,
    val phoneNumber: String? = null,
    val profile: AuthUserProfileDto? = null,
    val newUser: Boolean = false,
)

@Serializable
data class AuthUserProfileDto(
    val username: String? = null,
    val displayName: String? = null,
    val email: String? = null,
    val onboardingCompleted: Boolean = false,
)
