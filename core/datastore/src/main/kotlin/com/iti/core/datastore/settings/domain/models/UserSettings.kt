package com.iti.core.datastore.settings.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class UserSettings(
    val language: LanguageSetting = LanguageSetting.ENGLISH,
    val theme: ThemeSetting = ThemeSetting.FOLLOW_SYSTEM,
)
