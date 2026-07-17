package com.iti.careerpilot.editprofile.presentation.screen.models

import androidx.annotation.StringRes
import com.iti.careerpilot.profile.R

enum class EducationLevel(
    @param:StringRes val labelRes: Int
) {
    HIGH_SCHOOL(R.string.education_high_school),
    ASSOCIATE(R.string.education_associate),
    BACHELORS(R.string.education_bachelors),
    MASTERS(R.string.education_masters),
    DOCTORATE(R.string.education_doctorate),
    OTHER(R.string.education_other)
}