package com.iti.careerpilot.challengedetails.data.remote

import com.iti.common.error.FirebaseError
import com.iti.common.result.CareerPilotResult
import com.iti.core.model.Challenge

interface ChallengeDetailsRemoteDataSource {
    suspend fun getChallenge(challengeId: String): CareerPilotResult<Challenge, FirebaseError>
}
