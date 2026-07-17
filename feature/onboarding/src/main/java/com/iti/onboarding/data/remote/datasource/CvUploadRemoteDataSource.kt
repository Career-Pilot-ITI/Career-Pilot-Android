package com.iti.onboarding.data.remote.datasource

import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.onboarding.domain.model.CvDocument

interface CvUploadRemoteDataSource {
    suspend fun uploadCv(
        document: CvDocument,
        onProgress: (Float) -> Unit,
    ): CareerPilotResult<Unit, NetworkError>
}
