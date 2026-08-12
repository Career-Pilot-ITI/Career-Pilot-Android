package com.iti.careerpilot.features.settings.domain

import com.iti.careerpilot.features.settings.domain.models.UserSettings
import kotlinx.coroutines.flow.Flow

interface UserSettingsRepo {
    val settingsFlow: Flow<UserSettings>
    suspend fun updateUserSettings(updateBlock: (UserSettings) -> UserSettings)
}