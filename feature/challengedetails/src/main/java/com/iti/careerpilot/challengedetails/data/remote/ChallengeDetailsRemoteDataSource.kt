package com.iti.careerpilot.challengedetails.data.remote

import com.iti.careerpilot.challengefirestore.Challenge
import com.iti.common.error.FirebaseError
import com.iti.common.result.CareerPilotResult

interface ChallengeDetailsRemoteDataSource {
    suspend fun getChallenge(challengeId: String): CareerPilotResult<Challenge, FirebaseError>
}
