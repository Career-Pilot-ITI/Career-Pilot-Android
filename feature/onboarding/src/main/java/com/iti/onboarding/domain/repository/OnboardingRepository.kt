package com.iti.onboarding.domain.repository

import android.net.Uri
import com.iti.careerpilot.core.network.model.UpdateProfileRequestDto
import com.iti.careerpilot.core.network.model.UserProfileDto
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.core.datastore.models.UserProfile
import com.iti.onboarding.domain.model.Track
import com.iti.onboarding.domain.model.UploadedFile
import kotlinx.coroutines.flow.StateFlow

interface OnboardingRepository {
    val userProfile: StateFlow<UserProfile>
    suspend fun uploadFile(
        uri: Uri,
        onProgress: (Int) -> Unit
    ): CareerPilotResult<UploadedFile, NetworkError>

    suspend fun saveAvatarUrl(file: UploadedFile)
    suspend fun updateProfile(request: UpdateProfileRequestDto): CareerPilotResult<UserProfileDto, NetworkError>
    suspend fun getTracks(): CareerPilotResult<List<Track>, NetworkError>
    suspend fun uploadCv(
        uri: Uri,
        onProgress: (Int) -> Unit
    ): CareerPilotResult<UploadedFile, NetworkError>

    suspend fun updateProfileTrack(trackId: Long): CareerPilotResult<Unit, NetworkError>

    suspend fun completeOnboarding(cvFileId: Long?): CareerPilotResult<Unit, NetworkError>
}
