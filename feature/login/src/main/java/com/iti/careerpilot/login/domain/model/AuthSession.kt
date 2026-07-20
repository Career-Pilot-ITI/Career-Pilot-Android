package com.iti.careerpilot.login.domain.model

data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val expiresInMillis: Long,
    val userId: Long,
    val username: String?,
    val hasCompletedOnboarding: Boolean,
)
