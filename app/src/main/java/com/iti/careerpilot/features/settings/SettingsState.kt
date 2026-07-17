package com.iti.careerpilot.features.settings

data class SettingsState(
    val paramOne: String = "default",
    val paramTwo: List<String> = emptyList(),
)