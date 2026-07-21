package com.iti.careerpilot.login.data.mapper

import com.iti.careerpilot.login.data.remote.dto.OtpAuthResponse
import com.iti.careerpilot.login.domain.model.AuthSession

fun OtpAuthResponse.toDomain(): AuthSession =
    AuthSession(
        accessToken = authTokens.accessToken,
        refreshToken = authTokens.refreshToken,
        expiresInMillis = authTokens.expiresIn,
        userId = user.id,
        username = user.profile?.username,
        hasCompletedOnboarding = user.profile?.onboardingCompleted == true,
    )
