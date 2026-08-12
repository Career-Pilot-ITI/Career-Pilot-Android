package com.iti.careerpilot.features.settings.domain.models

import com.iti.careerpilot.R
import kotlinx.serialization.Serializable

@Serializable
enum class LanguageSetting {
    ENGLISH,
    ARABIC;
    fun getCode(): String {
        return when (this) {
            ENGLISH -> "en"
            ARABIC -> "ar"
        }
    }
    fun getTitleId(): Int {
        return when (this) {
            ENGLISH -> R.string.english
            ARABIC -> R.string.arabic
        }
    }
}