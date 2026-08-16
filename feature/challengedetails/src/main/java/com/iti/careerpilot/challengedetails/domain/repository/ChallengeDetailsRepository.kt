package com.iti.careerpilot.challengedetails.domain.repository

import com.iti.careerpilot.challengefirestore.Challenge
import com.iti.common.error.FirebaseError
import com.iti.common.result.CareerPilotResult

interface ChallengeDetailsRepository {
    suspend fun getChallenge(challengeId: String): CareerPilotResult<Challenge, FirebaseError>
}
