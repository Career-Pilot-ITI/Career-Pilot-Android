package com.iti.onboarding.data.local.datasource

import com.iti.common.error.StorageError
import com.iti.common.result.CareerPilotResult
import com.iti.onboarding.domain.model.CvDocument

interface CvFileLocalDataSource {
    suspend fun readPdf(uri: String): CareerPilotResult<CvDocument, StorageError>
}
