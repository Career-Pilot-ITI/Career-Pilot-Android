package com.iti.common.error

enum class NetworkError : IError {
    TIME_OUT,
    TOO_MANY_REQUESTS,
    NO_INTERNET,
    SERVER,
    SERIALIZATION,
    EMPTY_RESULT,
    BAD_REQUEST,
    UNAUTHORIZED,
    FORBIDDEN,
    NOT_FOUND,
    CONFLICT,
    GONE,
    OTP_EXPIRED,
    INVALID_COUPON,
    ADDRESS_ERROR,
    UNKNOWN,
}

enum class StorageError : IError {
    FILE_TOO_LARGE,
    INCOMPATIBLE_FILE,
    UNKNOWN,
    FileNotFound,
    PermissionDenied
}

enum class PhoneValidationError : IError {
    EMPTY,
    TOO_SHORT,
    TOO_LONG,
    INVALID_FORMAT,
}
