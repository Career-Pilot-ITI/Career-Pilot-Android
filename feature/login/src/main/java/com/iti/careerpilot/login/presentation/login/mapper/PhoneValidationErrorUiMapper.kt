package com.iti.careerpilot.login.presentation.login.mapper

import com.iti.careerpilot.login.R
import com.iti.common.error.PhoneValidationError
import com.iti.common.util.UIText

fun PhoneValidationError.toUIText(): UIText = UIText.StringResource(
    resId = when (this) {
        PhoneValidationError.EMPTY -> R.string.error_phone_empty
        PhoneValidationError.TOO_SHORT -> R.string.error_phone_too_short
        PhoneValidationError.TOO_LONG -> R.string.error_phone_too_long
        PhoneValidationError.INVALID_FORMAT -> R.string.error_phone_invalid
    }
)
