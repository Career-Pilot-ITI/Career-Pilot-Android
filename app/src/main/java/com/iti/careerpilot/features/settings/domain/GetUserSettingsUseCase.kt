package com.iti.careerpilot.features.settings.domain

import com.iti.careerpilot.features.settings.domain.models.UserSettings
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserSettingsUseCase @Inject constructor(
    private val userSettingsRepo: UserSettingsRepo,
) {
    operator fun invoke(): Flow<UserSettings> = userSettingsRepo.settingsFlow
}
