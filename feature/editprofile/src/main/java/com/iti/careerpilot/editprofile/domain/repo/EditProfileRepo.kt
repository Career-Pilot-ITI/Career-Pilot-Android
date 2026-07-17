package com.iti.careerpilot.editprofile.domain.repo

import android.net.Uri
import com.iti.careerpilot.editprofile.data.datasource.remote.models.FileUploadResponse
import com.iti.careerpilot.editprofile.domain.models.RequestProfileUpdate
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.core.datastore.models.UserProfile
import kotlinx.coroutines.flow.Flow

interface EditProfileRepo {

    val userProfile: Flow<UserProfile>

    suspend fun updateProfile(
        request: RequestProfileUpdate
    )

    suspend fun uploadImage(
        uri: Uri,
        onProgress: (Int) -> Unit
    ): CareerPilotResult<FileUploadResponse, NetworkError>
}