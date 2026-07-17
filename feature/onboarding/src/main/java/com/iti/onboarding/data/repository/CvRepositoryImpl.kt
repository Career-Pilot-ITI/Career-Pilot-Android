package com.iti.onboarding.data.repository

import com.iti.common.error.NetworkError
import com.iti.common.error.StorageError
import com.iti.common.result.CareerPilotResult
import com.iti.onboarding.data.local.datasource.CvFileLocalDataSource
import com.iti.onboarding.data.remote.datasource.CvUploadRemoteDataSource
import com.iti.onboarding.domain.model.CvDocument
import com.iti.onboarding.domain.repository.CvRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CvRepositoryImpl @Inject constructor(
    private val localDataSource: CvFileLocalDataSource,
    private val remoteDataSource: CvUploadRemoteDataSource,
) : CvRepository {

    override suspend fun preparePdf(
        uri: String,
    ): CareerPilotResult<CvDocument, StorageError> {
        return localDataSource.readPdf(uri)
    }

    override suspend fun uploadCv(
        document: CvDocument,
        onProgress: (Float) -> Unit,
    ): CareerPilotResult<Unit, NetworkError> {
        return remoteDataSource.uploadCv(
            document = document,
            onProgress = onProgress,
        )
    }
}
