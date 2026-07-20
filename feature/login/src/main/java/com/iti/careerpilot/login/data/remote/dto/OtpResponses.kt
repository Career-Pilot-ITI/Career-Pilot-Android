package com.iti.careerpilot.login.data.remote.dto

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
data class AuthTokensDto(
    @SerialName("accessToken") val accessToken: String,
    @SerialName("refreshToken") val refreshToken: String,
    @SerialName("expiresIn") val expiresIn: Long = 0L,
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
    @SerialName("onboardingCompleted") val onboardingCompleted: Boolean = false,
)
