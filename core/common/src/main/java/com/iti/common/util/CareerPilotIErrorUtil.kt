package com.iti.common.util

import com.iti.common.R
import com.iti.common.error.FirebaseError
import com.iti.common.error.NetworkError
import com.iti.common.error.StorageError
import com.iti.common.error.TranscriptionError


fun NetworkError.toUIText(): UIText {
    return UIText.StringResource(
        resId = when (this) {
            NetworkError.TIME_OUT ->
                R.string.error_network_timeout

            NetworkError.TOO_MANY_REQUESTS ->
                R.string.error_too_many_requests

            NetworkError.NO_INTERNET ->
                R.string.error_no_internet

            NetworkError.SERVER ->
                R.string.error_server

            NetworkError.SERIALIZATION ->
                R.string.error_serialization

            NetworkError.EMPTY_RESULT ->
                R.string.error_empty_result

            NetworkError.BAD_REQUEST ->
                R.string.error_bad_request

            NetworkError.INSUFFICIENT_COINS ->
                R.string.error_insufficient_coins

            NetworkError.UNAUTHORIZED ->
                R.string.error_unauthorized

            NetworkError.FORBIDDEN ->
                R.string.error_forbidden

            NetworkError.NOT_FOUND ->
                R.string.error_not_found

            NetworkError.CONFLICT ->
                R.string.error_email_already_in_use

            NetworkError.GONE ->
                R.string.error_gone

            NetworkError.OTP_EXPIRED ->
                R.string.error_otp_expired

            NetworkError.INVALID_COUPON ->
                R.string.error_invalid_coupon

            NetworkError.ADDRESS_ERROR ->
                R.string.error_address

            NetworkError.UNKNOWN ->
                R.string.error_unknown

            NetworkError.FAKE_SERVER_ERROR ->
                R.string.fake_error_unknown
        }
    )
}

fun StorageError.toUIText(): UIText {
    return UIText.StringResource(
        resId = when (this) {
            StorageError.FILE_TOO_LARGE ->
                R.string.error_file_too_large

            StorageError.INCOMPATIBLE_FILE ->
                R.string.error_incompatible_file

            StorageError.UNKNOWN ->
                R.string.error_storage_unknown

            StorageError.FileNotFound ->
                R.string.error_storage_file_not_found

            StorageError.PermissionDenied ->
                R.string.error_storage_permission_denied
        }
    )
}

fun TranscriptionError.toUIText(): UIText {
    return UIText.StringResource(
        resId = when (this) {
            TranscriptionError.UNKNOWN ->
                R.string.error_network_timeout
        }
    )
}

fun FirebaseError.toUIText(): UIText {
    return UIText.StringResource(
        resId = when (this) {
            FirebaseError.SERVICE_ERROR -> R.string.error_firebase
            FirebaseError.BAD_RESPONSE -> R.string.error_empty_response
            FirebaseError.QUOTA_EXCEEDED -> R.string.error_ai_quota_exceeded
            FirebaseError.PROMPT_BLOCKED -> R.string.error_ai_prompt_blocked
            FirebaseError.RESPONSE_STOPPED -> R.string.error_ai_response_stopped
            FirebaseError.TIMEOUT -> R.string.error_ai_timeout
            FirebaseError.NOT_FOUND -> R.string.error_not_found
            FirebaseError.BAD_REQUEST -> R.string.error_bad_request
            FirebaseError.UNKNOWN -> R.string.error_unknown
            FirebaseError.FAKE_ERROR -> R.string.fake_error_unknown
        }
    )
}
