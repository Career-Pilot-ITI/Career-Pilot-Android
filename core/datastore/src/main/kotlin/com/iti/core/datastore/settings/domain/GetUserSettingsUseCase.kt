package com.iti.core.datastore.settings.domain

import com.iti.core.datastore.settings.domain.models.UserSettings
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserSettingsUseCase @Inject constructor(
    private val userSettingsRepo: UserSettingsRepo,
) {
    operator fun invoke(): Flow<UserSettings> = userSettingsRepo.settingsFlow
}
