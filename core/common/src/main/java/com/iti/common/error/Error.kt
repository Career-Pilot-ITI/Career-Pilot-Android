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
    FAKE_SERVER_ERROR
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

enum class TranscriptionError : IError {
    UNKNOWN
}

enum class FirebaseError : IError {
    BAD_RESPONSE,
    SERVICE_ERROR,
    QUOTA_EXCEEDED,
    PROMPT_BLOCKED,
    RESPONSE_STOPPED,
    TIMEOUT,
    UNKNOWN,
}
