package com.iti.careerpilot.login.domain.usecase

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat
import com.google.i18n.phonenumbers.PhoneNumberUtil.ValidationResult
import com.iti.common.error.PhoneValidationError
import com.iti.common.result.CareerPilotResult
import javax.inject.Inject

class ValidatePhoneNumberUseCase @Inject constructor(
    private val phoneNumberUtil: PhoneNumberUtil,
) {

    operator fun invoke(
        rawNumber: String,
        regionCode: String,
    ): CareerPilotResult<String, PhoneValidationError> {
        if (rawNumber.isBlank()) {
            return CareerPilotResult.Error(PhoneValidationError.EMPTY)
        }

        return try {
            val parsed = phoneNumberUtil.parse(rawNumber, regionCode.uppercase())

            if (phoneNumberUtil.isValidNumber(parsed)) {
                CareerPilotResult.Success(phoneNumberUtil.format(parsed, PhoneNumberFormat.E164))
            } else {
                CareerPilotResult.Error(
                    when (phoneNumberUtil.isPossibleNumberWithReason(parsed)) {
                        ValidationResult.TOO_SHORT,
                        ValidationResult.IS_POSSIBLE_LOCAL_ONLY -> PhoneValidationError.TOO_SHORT

                        ValidationResult.TOO_LONG -> PhoneValidationError.TOO_LONG

                        else -> PhoneValidationError.INVALID_FORMAT
                    }
                )
            }
        } catch (e: NumberParseException) {
            CareerPilotResult.Error(
                when (e.errorType) {
                    NumberParseException.ErrorType.TOO_SHORT_NSN,
                    NumberParseException.ErrorType.TOO_SHORT_AFTER_IDD -> PhoneValidationError.TOO_SHORT

                    NumberParseException.ErrorType.TOO_LONG -> PhoneValidationError.TOO_LONG

                    else -> PhoneValidationError.INVALID_FORMAT
                }
            )
        }
    }
}
