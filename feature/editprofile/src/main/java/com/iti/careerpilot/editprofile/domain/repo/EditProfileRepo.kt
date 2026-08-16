package com.iti.careerpilot.editprofile.domain.repo

import android.net.Uri
import com.iti.careerpilot.editprofile.data.datasource.remote.models.FileUploadResponse
import com.iti.careerpilot.editprofile.domain.models.RequestProfileUpdate
import com.iti.core.model.Track
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.core.datastore.models.UserProfile
import kotlinx.coroutines.flow.StateFlow

interface EditProfileRepo {

    val userProfile: StateFlow<UserProfile>

    suspend fun updateProfile(
        request: RequestProfileUpdate
    ): CareerPilotResult<Unit, NetworkError>

    suspend fun uploadImage(
        uri: Uri,
        onProgress: (Int) -> Unit
    ): CareerPilotResult<FileUploadResponse, NetworkError>

    suspend fun uploadAndAnalyzeCV(
        uri: Uri,
        onUploadProgress: (Int) -> Unit,
        onAnalysisStarted: () -> Unit,
    ): CareerPilotResult<FileUploadResponse, NetworkError>

    suspend fun ensureCvAvailableLocally()

    suspend fun getTracks(): CareerPilotResult<List<Track>, NetworkError>
}