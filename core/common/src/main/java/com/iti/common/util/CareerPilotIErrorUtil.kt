package com.iti.common.util

import com.iti.common.R
import com.iti.common.error.NetworkError
import com.iti.common.error.StorageError

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

            NetworkError.UNAUTHORIZED ->
                R.string.error_unauthorized

            NetworkError.FORBIDDEN ->
                R.string.error_forbidden

            NetworkError.NOT_FOUND ->
                R.string.error_not_found

            NetworkError.CONFLICT ->
                R.string.error_conflict

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