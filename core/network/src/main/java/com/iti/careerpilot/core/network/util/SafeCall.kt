package com.iti.careerpilot.core.network.util

import android.util.Log
import com.iti.careerpilot.core.safecall.TAG
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
        Log.e(TAG, "KtorClient: ", e)
        return CareerPilotResult.Error(e.toNetworkError())
    } catch (e: ClientRequestException) {
        Log.e(TAG, "KtorClient: ", e)
        return CareerPilotResult.Error(e.toNetworkError())
    } catch (e: ServerResponseException) {
        Log.e(TAG, "KtorClient: ", e)
        return CareerPilotResult.Error(e.toNetworkError())
    } catch (e: HttpRequestTimeoutException) {
        Log.e(TAG, "KtorClient: ", e)
        return CareerPilotResult.Error(NetworkError.TIME_OUT)
    } catch (e: ConnectTimeoutException) {
        Log.e(TAG, "KtorClient: ", e)
        return CareerPilotResult.Error(NetworkError.TIME_OUT)
    } catch (e: SocketTimeoutException) {
        Log.e(TAG, "KtorClient: ", e)
        return CareerPilotResult.Error(NetworkError.TIME_OUT)
    } catch (e: UnresolvedAddressException) {
        Log.e(TAG, "KtorClient: ", e)
        return CareerPilotResult.Error(NetworkError.NO_INTERNET)
    } catch (e: UnknownHostException) {
        Log.e(TAG, "KtorClient: ", e)
        return CareerPilotResult.Error(NetworkError.NO_INTERNET)
    } catch (e: IOException) { // connection reset / refused / dropped
        Log.e(TAG, "KtorClient: ", e)
        return CareerPilotResult.Error(NetworkError.NO_INTERNET)
    } catch (e: CancellationException) {
        Log.e(TAG, "KtorClient: ", e)
        throw e
    } catch (e: Exception) {
        Log.e(TAG, "KtorClient: ", e)
        return CareerPilotResult.Error(NetworkError.UNKNOWN)
    }

    return response.toResult()
}

suspend inline fun <reified T> HttpResponse.toResult(): CareerPilotResult<T, NetworkError> {
    return try {
        CareerPilotResult.Success(body<T>())
    } catch (e: SerializationException) {
        Log.e(TAG, "KtorClient: ", e)
        CareerPilotResult.Error(NetworkError.SERIALIZATION)
    } catch (e: ContentConvertException) {
        Log.e(TAG, "KtorClient: ", e)
        CareerPilotResult.Error(NetworkError.SERIALIZATION)
    } catch (e: CancellationException) {
        Log.e(TAG, "KtorClient: ", e)
        throw e
    } catch (e: Exception) {
        Log.e(TAG, "KtorClient: ", e)
        CareerPilotResult.Error(NetworkError.UNKNOWN)
    }
}

fun ResponseException.toNetworkError(): NetworkError = when (response.status.value) {
    400 -> NetworkError.BAD_REQUEST
    401 -> NetworkError.UNAUTHORIZED
    402 -> NetworkError.INSUFFICIENT_COINS
    403 -> NetworkError.FORBIDDEN
    404 -> NetworkError.NOT_FOUND
    409 -> NetworkError.CONFLICT
    410 -> NetworkError.GONE
    429 -> NetworkError.TOO_MANY_REQUESTS
    in 500..599 -> NetworkError.SERVER
    else -> NetworkError.UNKNOWN
}
