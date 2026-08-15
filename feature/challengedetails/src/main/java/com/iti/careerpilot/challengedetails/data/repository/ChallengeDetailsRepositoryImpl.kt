package com.iti.careerpilot.challengedetails.data.repository

import com.iti.careerpilot.challengedetails.data.remote.ChallengeDetailsRemoteDataSource
import com.iti.careerpilot.challengedetails.domain.repository.ChallengeDetailsRepository
import com.iti.common.error.FirebaseError
import com.iti.common.result.CareerPilotResult
import com.iti.core.model.Challenge
import javax.inject.Inject

class ChallengeDetailsRepositoryImpl @Inject constructor(
    private val remoteDataSource: ChallengeDetailsRemoteDataSource
) : ChallengeDetailsRepository {
    override suspend fun getChallenge(challengeId: String): CareerPilotResult<Challenge, FirebaseError> {
        return remoteDataSource.getChallenge(challengeId)
    }
}
