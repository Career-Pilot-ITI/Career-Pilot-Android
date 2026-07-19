package com.iti.careerpilot.editprofile.presentation.screen.models

import androidx.annotation.StringRes
import com.iti.careerpilot.editprofile.R

enum class Gender(
    @param:StringRes val labelRes: Int,
    val backendValue: String
) {
    MALE(R.string.gender_male, "male"),
    FEMALE(R.string.gender_female, "female");

    companion object {
        fun fromBackendValue(value: String): Gender? = entries.find { it.backendValue == value.lowercase() }
    }
}