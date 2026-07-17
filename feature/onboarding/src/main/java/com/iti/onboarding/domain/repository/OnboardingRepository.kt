package com.iti.onboarding.domain.repository

import com.iti.common.result.CareerPilotResult
import com.iti.onboarding.domain.error.TracksScreenError
import com.iti.onboarding.domain.model.Track

interface OnboardingRepository {
    suspend fun getTracksUseCase(): CareerPilotResult<List<Track>, TracksScreenError>
}