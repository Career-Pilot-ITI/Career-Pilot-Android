package com.iti.core.datastore.settings.domain.models

import kotlinx.serialization.Serializable

@Serializable
enum class ThemeSetting {
    LIGHT,
    DARK,
    FOLLOW_SYSTEM;
}
