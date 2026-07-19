package com.iti.careerpilot.core.safecall

import android.util.Log
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.statement.HttpResponse
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

const val TAG = "CareerPilot: SafeRestCall"

suspend inline fun <reified T> safeApiCall(
    execute: () -> HttpResponse,
): CareerPilotResult<T, NetworkError> {
    val response = try {
        execute()
    } catch (_: SocketTimeoutException) {
        return CareerPilotResult.Error(NetworkError.TIME_OUT)
    } catch (_: UnresolvedAddressException) {
        return CareerPilotResult.Error(NetworkError.NO_INTERNET)
    } catch (e: Exception) {
        currentCoroutineContext().ensureActive()
        Log.e(TAG, "safeCall: ", e)
        return CareerPilotResult.Error(NetworkError.UNKNOWN)
    }

    return responseToCareerPilotResult(response)
}

suspend inline fun <reified T> responseToCareerPilotResult(
    response: HttpResponse,
): CareerPilotResult<T, NetworkError> {
    return when (response.status.value) {
        in 200..299 -> {
            try {
                CareerPilotResult.Success(response.body<T>())
            } catch (_: NoTransformationFoundException) {
                CareerPilotResult.Error(NetworkError.SERIALIZATION)
            }
        }

        408 -> CareerPilotResult.Error(NetworkError.TIME_OUT)
        429 -> CareerPilotResult.Error(NetworkError.TOO_MANY_REQUESTS)
        in 500..599 -> CareerPilotResult.Error(NetworkError.SERVER)
        else -> CareerPilotResult.Error(NetworkError.UNKNOWN)
    }
}