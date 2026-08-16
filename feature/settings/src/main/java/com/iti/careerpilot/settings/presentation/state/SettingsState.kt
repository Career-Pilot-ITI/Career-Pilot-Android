package com.iti.careerpilot.settings.presentation.state

data class SettingsState(
    val showLanguageDialog: Boolean = false,
    val showThemeDialog: Boolean = false,
    val planDisplayName: String = "Free",
    val isMaxPlan: Boolean = false,
    val coinBalance: Int = 0,
)