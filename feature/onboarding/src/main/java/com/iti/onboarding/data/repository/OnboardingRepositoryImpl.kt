package com.iti.onboarding.data.repository

import com.iti.common.result.CareerPilotResult
import com.iti.onboarding.data.local.datasource.OnboardingLocalDataSource
import com.iti.onboarding.data.remote.datasource.OnboardingRemoteDataSource
import com.iti.onboarding.domain.error.TracksScreenError
import com.iti.onboarding.domain.model.Track
import com.iti.onboarding.domain.repository.OnboardingRepository
import jakarta.inject.Inject

class OnboardingRepositoryImpl @Inject constructor(
    private val remoteDataSource: OnboardingRemoteDataSource,
    private val localDataSource: OnboardingLocalDataSource,
): OnboardingRepository {

    override suspend fun getTracksUseCase(): CareerPilotResult<List<Track>, TracksScreenError> {
        return remoteDataSource.getTracksUseCase()
    }
}