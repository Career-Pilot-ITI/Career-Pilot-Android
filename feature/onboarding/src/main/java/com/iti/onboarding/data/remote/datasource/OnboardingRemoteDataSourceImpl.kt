package com.iti.onboarding.data.remote.datasource

import com.iti.common.result.CareerPilotResult
import com.iti.onboarding.domain.error.TracksScreenError
import com.iti.onboarding.domain.model.Track
import com.iti.onboarding.util.dummyTracks

class OnboardingRemoteDataSourceImpl(
    // Inject services
): OnboardingRemoteDataSource {

    override fun getTracksUseCase(): CareerPilotResult<List<Track>, TracksScreenError> {
        // call service

        val result = dummyTracks
        return CareerPilotResult.Success(result)
    }
}