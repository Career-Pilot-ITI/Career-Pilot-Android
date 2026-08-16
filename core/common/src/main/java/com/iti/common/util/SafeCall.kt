package com.iti.common.util

import android.util.Log
import com.iti.common.error.FirebaseError
import com.iti.common.result.CareerPilotResult
import com.google.firebase.ai.type.FirebaseAIException
import com.google.firebase.ai.type.PromptBlockedException
import com.google.firebase.ai.type.QuotaExceededException
import com.google.firebase.ai.type.RequestTimeoutException
import com.google.firebase.ai.type.ResponseStoppedException
import com.google.firebase.ai.type.SerializationException
import com.google.firebase.ai.type.ServerException
import kotlinx.coroutines.CancellationException

suspend inline fun <reified T> safeFirebaseCall(
    call: suspend () -> T
): CareerPilotResult<T, FirebaseError> {
    return try {
        val response = call()
        CareerPilotResult.Success(response)
    } catch (e: PromptBlockedException) {
        Log.e("SafeFirebaseCall", "Prompt blocked", e)
        CareerPilotResult.Error(FirebaseError.PROMPT_BLOCKED)
    } catch (e: ResponseStoppedException) {
        Log.e("SafeFirebaseCall", "Response stopped", e)
        CareerPilotResult.Error(FirebaseError.RESPONSE_STOPPED)
    } catch (e: QuotaExceededException) {
        Log.e("SafeFirebaseCall", "Quota exceeded", e)
        CareerPilotResult.Error(FirebaseError.QUOTA_EXCEEDED)
    } catch (e: RequestTimeoutException) {
        Log.e("SafeFirebaseCall", "Request timeout", e)
        CareerPilotResult.Error(FirebaseError.TIMEOUT)
    } catch (e: ServerException) {
        Log.e("SafeFirebaseCall", "Server exception", e)
        CareerPilotResult.Error(FirebaseError.SERVICE_ERROR)
    } catch (e: SerializationException) {
        Log.e("SafeFirebaseCall", "Serialization exception", e)
        CareerPilotResult.Error(FirebaseError.BAD_RESPONSE)
    } catch (e: FirebaseAIException) {
        Log.e("SafeFirebaseCall", "Firebase AI exception", e)
        CareerPilotResult.Error(FirebaseError.SERVICE_ERROR)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.e("SafeFirebaseCall", "Unknown exception", e)
        CareerPilotResult.Error(FirebaseError.UNKNOWN)
    }
}
