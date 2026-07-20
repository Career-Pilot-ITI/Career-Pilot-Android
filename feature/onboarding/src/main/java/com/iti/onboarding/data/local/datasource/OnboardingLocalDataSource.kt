package com.iti.onboarding.data.local.datasource

interface OnboardingLocalDataSource {
    suspend fun saveAvatarUrl(url: String)
    suspend fun savePdfUrl(url: String)
}