package com.iti.careerpilot.features.settings.presentation.action

import com.iti.careerpilot.features.settings.domain.models.ThemeSetting


sealed interface SettingsAction {
    data class UpdateTheme(
        val theme: ThemeSetting
    ) : SettingsAction

    data class LanguageDialogToggle(val open: Boolean) : SettingsAction
    data class ThemeDialogToggle(val open: Boolean) : SettingsAction

}