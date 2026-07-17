package com.iti.careerpilot.editprofile.presentation.screen.models

import androidx.annotation.StringRes
import com.iti.careerpilot.profile.R

enum class ExperienceLevel(
    @param:StringRes val labelRes: Int
) {
    ENTRY_LEVEL(R.string.experience_entry_level),
    JUNIOR(R.string.experience_junior),
    MID_LEVEL(R.string.experience_mid_level),
    SENIOR(R.string.experience_senior),
    LEAD(R.string.experience_lead),
    EXECUTIVE(R.string.experience_executive)
}