package com.iti.careerpilot.core.result

import com.iti.careerpilot.core.error.CareerPilotIError


sealed interface CareerPilotResult<out D, out E : CareerPilotIError> {
    data class Success<out D>(val data: D) : CareerPilotResult<D, Nothing>
    data class Error<out E : CareerPilotIError>(val error: E) : CareerPilotResult<Nothing, E>
}

typealias EmptyResult = CareerPilotResult<Unit, CareerPilotIError>


inline fun <D, E : CareerPilotIError> CareerPilotResult<D, E>.onSuccess(
    action: (D) -> Unit
): CareerPilotResult<D, E> {
    when (this) {
        is CareerPilotResult.Success -> action(data)
        is CareerPilotResult.Error -> Unit
    }

    return this
}

inline fun <D, E : CareerPilotIError> CareerPilotResult<D, E>.onError(
    action: (E) -> Unit
): CareerPilotResult<D, E> {
    when (this) {
        is CareerPilotResult.Error -> action(error)
        is CareerPilotResult.Success -> Unit
    }

    return this
}