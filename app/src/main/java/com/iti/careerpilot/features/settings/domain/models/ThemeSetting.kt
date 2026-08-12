package com.iti.careerpilot.features.settings.domain.models

import com.iti.careerpilot.R
import kotlinx.serialization.Serializable

@Serializable
enum class ThemeSetting {
    LIGHT,
    DARK,
    FOLLOW_SYSTEM;

    fun getTitleId(): Int {
        return when (this) {
            LIGHT -> R.string.light
            DARK -> R.string.dark
            FOLLOW_SYSTEM -> R.string.follow_system
        }
    }
}
