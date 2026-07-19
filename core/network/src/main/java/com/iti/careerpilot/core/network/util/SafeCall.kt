package com.iti.careerpilot.core.network.util

import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import io.ktor.client.call.body
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.HttpResponse
import io.ktor.serialization.ContentConvertException
import java.io.IOException
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.SerializationException

suspend inline fun <reified T> safeCall(
    execute: () -> HttpResponse,
): CareerPilotResult<T, NetworkError> {
    val response = try {
        execute()
    } catch (e: RedirectResponseException) {
        return CareerPilotResult.Error(e.toNetworkError())
    } catch (e: ClientRequestException) {
        return CareerPilotResult.Error(e.toNetworkError())
    } catch (e: ServerResponseException) {
        return CareerPilotResult.Error(e.toNetworkError())
    } catch (e: HttpRequestTimeoutException) {
        return CareerPilotResult.Error(NetworkError.TIME_OUT)
    } catch (e: ConnectTimeoutException) {
        return CareerPilotResult.Error(NetworkError.TIME_OUT)
    } catch (e: SocketTimeoutException) {
        return CareerPilotResult.Error(NetworkError.TIME_OUT)
    } catch (e: UnresolvedAddressException) {
        return CareerPilotResult.Error(NetworkError.NO_INTERNET)
    } catch (e: UnknownHostException) {
        return CareerPilotResult.Error(NetworkError.NO_INTERNET)
    } catch (e: IOException) { // connection reset / refused / dropped
        return CareerPilotResult.Error(NetworkError.NO_INTERNET)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        return CareerPilotResult.Error(NetworkError.UNKNOWN)
    }

    return response.toResult()
}

suspend inline fun <reified T> HttpResponse.toResult(): CareerPilotResult<T, NetworkError> {
    return try {
        CareerPilotResult.Success(body<T>())
    } catch (e: SerializationException) {
        CareerPilotResult.Error(NetworkError.SERIALIZATION)
    } catch (e: ContentConvertException) {
        CareerPilotResult.Error(NetworkError.SERIALIZATION)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        CareerPilotResult.Error(NetworkError.UNKNOWN)
    }
}

fun ResponseException.toNetworkError(): NetworkError = when (response.status.value) {
    400 -> NetworkError.BAD_REQUEST
    401 -> NetworkError.UNAUTHORIZED
    403 -> NetworkError.FORBIDDEN
    404 -> NetworkError.NOT_FOUND
    409 -> NetworkError.CONFLICT
    410 -> NetworkError.GONE
    429 -> NetworkError.TOO_MANY_REQUESTS
    in 500..599 -> NetworkError.SERVER
    else -> NetworkError.UNKNOWN
}
