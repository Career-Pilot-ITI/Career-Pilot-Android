package com.iti.onboarding.presentation.screen.profileinfo.model

import androidx.annotation.StringRes
import com.iti.onboarding.R

enum class ExperienceLevel(
    @param:StringRes val labelRes: Int,
    val apiKey: String,
    val defaultYears: Int,
) {
    ENTRY_LEVEL(R.string.experience_entry_level, "Entry-level", 1),
    JUNIOR(R.string.experience_junior, "Junior", 2),
    MID_LEVEL(R.string.experience_mid_level, "Mid-level", 4),
    SENIOR(R.string.experience_senior, "Senior", 7),
    LEAD(R.string.experience_lead, "Lead", 10),
    EXECUTIVE(R.string.experience_executive, "Executive", 15);

    companion object {
        fun fromYears(years: Int): ExperienceLevel = when {
            years <= 1 -> ENTRY_LEVEL
            years in 2..3 -> JUNIOR
            years in 4..6 -> MID_LEVEL
            years in 7..9 -> SENIOR
            years in 10..14 -> LEAD
            years >= 15 -> EXECUTIVE
            else -> ENTRY_LEVEL
        }

        fun fromString(value: String): ExperienceLevel? {
            if (value.isBlank()) return null
            return entries.firstOrNull { level ->
                level.apiKey.equals(value, ignoreCase = true) ||
                    level.name.equals(value, ignoreCase = true) ||
                    value.contains(level.apiKey, ignoreCase = true)
            }
        }
    }
}
