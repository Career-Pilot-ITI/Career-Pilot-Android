package com.iti.careerpilot.settings.presentation.action

import com.iti.core.datastore.settings.domain.models.ThemeSetting

sealed interface SettingsIntent {
    data class UpdateTheme(
        val theme: ThemeSetting
    ) : SettingsIntent

    data class LanguageDialogToggle(val open: Boolean) : SettingsIntent
    data class ThemeDialogToggle(val open: Boolean) : SettingsIntent
    data object ManageSubscriptionClicked : SettingsIntent
    data object CoinsClicked : SettingsIntent
}
