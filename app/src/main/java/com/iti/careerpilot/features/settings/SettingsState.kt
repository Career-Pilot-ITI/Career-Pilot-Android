package com.iti.careerpilot.features.settings

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class SettingsState(
    val paramOne: String = "default",
    val paramTwo: ImmutableList<String> = persistentListOf(),
)