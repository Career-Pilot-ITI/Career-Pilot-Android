package com.iti.onboarding.data.local.datasource

import com.iti.core.datastore.CareerPilotPreferencesDataSource
import javax.inject.Inject

class OnboardingLocalDataSourceImpl @Inject constructor(
    private val preferencesDataSource: CareerPilotPreferencesDataSource
): OnboardingLocalDataSource {
    override suspend fun saveAvatarUrl(url: String) {
        preferencesDataSource.setAvatarUrl(url)
    }

    override suspend fun savePdfUrl(url: String) {
        preferencesDataSource.savePdfUrl(url)
    }
}