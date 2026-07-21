package com.iti.common.result

import com.iti.common.error.IError


sealed interface CareerPilotResult<out D, out E : IError> {
    data class Success<out D>(val data: D) : CareerPilotResult<D, Nothing>
    data class Error<out E : IError>(val error: E) : CareerPilotResult<Nothing, E>
}

typealias EmptyResult = CareerPilotResult<Unit, IError>


inline fun <D, E : IError> CareerPilotResult<D, E>.onSuccess(
    action: (D) -> Unit
): CareerPilotResult<D, E> {
    when (this) {
        is CareerPilotResult.Success -> action(data)
        is CareerPilotResult.Error -> Unit
    }

    return this
}

inline fun <D, E : IError> CareerPilotResult<D, E>.onError(
    action: (E) -> Unit
): CareerPilotResult<D, E> {
    when (this) {
        is CareerPilotResult.Error -> action(error)
        is CareerPilotResult.Success -> Unit
    }

    return this
}

fun <D, E : IError> CareerPilotResult<D, E>.mapToEmptyResult(): CareerPilotResult<Unit, E> {
    return when (this) {
        is CareerPilotResult.Success -> CareerPilotResult.Success(Unit)
        is CareerPilotResult.Error -> this
    }
}

inline fun <D, E : IError, T> CareerPilotResult<D, E>.map(
    action: (D) -> T
): CareerPilotResult<T, E> {
    return when (this) {
        is CareerPilotResult.Success -> CareerPilotResult.Success(action(data))
        is CareerPilotResult.Error -> this
    }
}