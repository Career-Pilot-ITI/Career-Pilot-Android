package com.iti.onboarding.data.remote.datasource

import com.iti.common.result.CareerPilotResult
import com.iti.onboarding.domain.error.TracksScreenError
import com.iti.onboarding.domain.model.Track

interface OnboardingRemoteDataSource {
    fun getTracksUseCase(): CareerPilotResult<List<Track>, TracksScreenError>
}