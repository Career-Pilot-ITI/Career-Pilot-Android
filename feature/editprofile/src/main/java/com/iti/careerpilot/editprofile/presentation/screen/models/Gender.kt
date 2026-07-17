package com.iti.careerpilot.editprofile.presentation.screen.models

import androidx.annotation.StringRes
import com.iti.careerpilot.editprofile.R

enum class Gender(
    @param:StringRes val labelRes: Int
) {
    MALE(R.string.gender_male),
    FEMALE(R.string.gender_female),
}