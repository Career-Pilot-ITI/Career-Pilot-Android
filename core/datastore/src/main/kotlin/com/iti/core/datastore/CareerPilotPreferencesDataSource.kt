package com.iti.core.datastore

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CareerPilotPreferencesDataSource @Inject constructor(
    private val userPreferences: DataStore<UserPreferences>,
) {
    val hasCompletedOnboarding: Flow<Boolean> =
        userPreferences.data.map { it.hasCompletedOnboarding }

    val token: Flow<String?> =
        userPreferences.data.map { it.token }

    val refreshToken: Flow<String?> =
        userPreferences.data.map { it.refreshToken }

    val avatarUrl: Flow<String?> =
        userPreferences.data.map { it.avatarUrl }

    suspend fun setHasCompletedOnboarding(completed: Boolean) {
        userPreferences.updateData { it.copy(hasCompletedOnboarding = completed) }
    }

    suspend fun setToken(token: String?) {
        userPreferences.updateData { it.copy(token = token) }
    }

    suspend fun setRefreshToken(refreshToken: String?) {
        userPreferences.updateData { it.copy(refreshToken = refreshToken) }
    }

    suspend fun setAvatarUrl(avatarUrl: String?) {
        userPreferences.updateData { it.copy(avatarUrl = avatarUrl) }
    }

    suspend fun savePdfUrl(url: String) {
        userPreferences.updateData { it.copy(pdfUrl = url) }
    }

    suspend fun setPdfInternalFileUri(uri: String) {
        userPreferences.updateData { it.copy(pdfInternalFileUri = uri) }
    }

    suspend fun clear() {
        userPreferences.updateData { UserPreferences() }
    }
}
