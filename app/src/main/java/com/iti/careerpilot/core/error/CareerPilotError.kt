package com.iti.careerpilot.core.error

sealed interface CareerPilotError: CareerPilotIError {
    enum class Network: CareerPilotError{
        TIME_OUT,
        TOO_MANY_REQUESTS,
        NO_INTERNET,
        SERVER,
        SERIALIZATION,
        EMPTY_RESULT,
        BAD_REQUEST,
        INVALID_COUPON,
        ADDRESS_ERROR,
        UNKNOWN,
    }

    enum class Storage: CareerPilotError{
        FILE_TOO_LARGE,
        INCOMPATIBLE_FILE,
        UNKNOWN
    }

    enum class Registration: CareerPilotError{
        EMAIL_ALREADY_IN_USE,
        WEAK_PASSWORD,
        NETWORK_ERROR,
        NO_INTERNET,
        UNKNOWN,
    }

    enum class SignIn: CareerPilotError{
        INVALID_CREDENTIALS,
        USER_NOT_FOUND,
        ACCOUNT_DISABLED,
        EMAIL_NOT_VERIFIED,
        NETWORK_ERROR,
        NO_INTERNET,
        INVALID_EMAIL,
        UNKNOWN,
    }
}