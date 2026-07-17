package com.iti.onboarding.domain.repository

import com.iti.common.error.NetworkError
import com.iti.common.error.StorageError
import com.iti.common.result.CareerPilotResult
import com.iti.onboarding.domain.model.CvDocument

interface CvRepository {
    suspend fun preparePdf(uri: String): CareerPilotResult<CvDocument, StorageError>

    suspend fun uploadCv(
        document: CvDocument,
        onProgress: (Float) -> Unit,
    ): CareerPilotResult<Unit, NetworkError>
}
