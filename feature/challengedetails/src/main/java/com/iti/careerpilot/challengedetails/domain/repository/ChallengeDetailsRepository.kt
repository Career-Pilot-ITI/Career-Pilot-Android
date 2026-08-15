package com.iti.careerpilot.challengedetails.domain.repository

import com.iti.common.error.FirebaseError
import com.iti.common.result.CareerPilotResult
import com.iti.core.model.Challenge

interface ChallengeDetailsRepository {
    suspend fun getChallenge(challengeId: String): CareerPilotResult<Challenge, FirebaseError>
}
