package com.iti.careerpilot.core.util

import com.iti.careerpilot.R
import com.iti.careerpilot.core.error.CareerPilotError

fun CareerPilotError.toUIText(): UIText {
    return UIText.StringResource(
        resId = when (this) {
            CareerPilotError.Network.TIME_OUT ->
                R.string.error_network_timeout

            CareerPilotError.Network.TOO_MANY_REQUESTS ->
                R.string.error_too_many_requests

            CareerPilotError.Network.NO_INTERNET ->
                R.string.error_no_internet

            CareerPilotError.Network.SERVER ->
                R.string.error_server

            CareerPilotError.Network.SERIALIZATION ->
                R.string.error_serialization

            CareerPilotError.Network.EMPTY_RESULT ->
                R.string.error_empty_result

            CareerPilotError.Network.BAD_REQUEST ->
                R.string.error_bad_request

            CareerPilotError.Network.INVALID_COUPON ->
                R.string.error_invalid_coupon

            CareerPilotError.Network.ADDRESS_ERROR ->
                R.string.error_address

            CareerPilotError.Network.UNKNOWN ->
                R.string.error_unknown

            CareerPilotError.Registration.EMAIL_ALREADY_IN_USE ->
                R.string.error_email_already_in_use

            CareerPilotError.Registration.WEAK_PASSWORD ->
                R.string.error_weak_password

            CareerPilotError.Registration.NETWORK_ERROR ->
                R.string.error_network

            CareerPilotError.Registration.NO_INTERNET ->
                R.string.error_no_internet

            CareerPilotError.Registration.UNKNOWN ->
                R.string.error_registration_unknown

            CareerPilotError.SignIn.INVALID_CREDENTIALS ->
                R.string.error_invalid_credentials

            CareerPilotError.SignIn.USER_NOT_FOUND ->
                R.string.error_user_not_found

            CareerPilotError.SignIn.ACCOUNT_DISABLED ->
                R.string.error_account_disabled

            CareerPilotError.SignIn.EMAIL_NOT_VERIFIED ->
                R.string.error_email_not_verified

            CareerPilotError.SignIn.NETWORK_ERROR ->
                R.string.error_network

            CareerPilotError.SignIn.NO_INTERNET ->
                R.string.error_no_internet

            CareerPilotError.SignIn.INVALID_EMAIL ->
                R.string.error_invalid_email

            CareerPilotError.SignIn.UNKNOWN ->
                R.string.error_sign_in_unknown

            CareerPilotError.Storage.FILE_TOO_LARGE ->
                R.string.error_file_too_large

            CareerPilotError.Storage.INCOMPATIBLE_FILE ->
                R.string.error_incompatible_file

            CareerPilotError.Storage.UNKNOWN ->
                R.string.error_storage_unknown
        }
    )
}