package com.iti.common.model

import androidx.annotation.StringRes
import com.iti.common.R

enum class ProfileEditSection(@param:StringRes val titleRes: Int) {
    PERSONAL(R.string.edit_personal_info),
    CAREER(R.string.edit_career_info),
    ALL(R.string.edit_profile)
}
