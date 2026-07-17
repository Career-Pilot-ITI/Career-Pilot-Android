package com.iti.onboarding.data.remote.datasource

import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.onboarding.domain.model.CvDocument
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay

@Singleton
class CvUploadRemoteDataSourceImpl @Inject constructor() : CvUploadRemoteDataSource {

    override suspend fun uploadCv(
        document: CvDocument,
        onProgress: (Float) -> Unit,
    ): CareerPilotResult<Unit, NetworkError> {
        /*
         * TODO: Inject the onboarding API service and replace this placeholder.
         *
         * The contract is already ready for a binary/multipart request. With
         * Ktor, forward HttpRequestBuilder.onUpload progress to onProgress.
         */
        val totalBytes = document.bytes.size.coerceAtLeast(1)
        var uploadedBytes = 0

        while (uploadedBytes < totalBytes) {
            delay(UPLOAD_PROGRESS_DELAY_MS)
            uploadedBytes = minOf(
                uploadedBytes + UPLOAD_CHUNK_SIZE_BYTES,
                totalBytes,
            )
            onProgress(uploadedBytes.toFloat() / totalBytes.toFloat())
        }

        return CareerPilotResult.Success(Unit)
    }

    private companion object {
        const val UPLOAD_CHUNK_SIZE_BYTES = 256 * 1024
        const val UPLOAD_PROGRESS_DELAY_MS = 40L
    }
}
