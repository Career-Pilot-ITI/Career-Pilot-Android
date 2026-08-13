package com.iti.core.datastore.settings.domain

import com.iti.core.datastore.settings.domain.models.UserSettings
import kotlinx.coroutines.flow.Flow

interface UserSettingsRepo {
    val settingsFlow: Flow<UserSettings>
    suspend fun updateUserSettings(updateBlock: (UserSettings) -> UserSettings)
}