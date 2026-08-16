package com.iti.careerpilot.challenges.data.repository

import com.iti.careerpilot.challenges.data.remote.ChallengesRemoteDataSource
import com.iti.careerpilot.challenges.domain.repository.ChallengesRepository
import com.iti.common.error.FirebaseError
import com.iti.common.result.CareerPilotResult
import com.iti.core.model.Challenge
import javax.inject.Inject

class ChallengesRepositoryImpl @Inject constructor(
    private val remoteDataSource: ChallengesRemoteDataSource
) : ChallengesRepository {

    override suspend fun getPublicChallenges(): CareerPilotResult<List<Challenge>, FirebaseError> {
        return remoteDataSource.getPublicChallenges()
    }

    override suspend fun checkChallengeExists(challengeId: String): CareerPilotResult<Boolean, FirebaseError> {
        return remoteDataSource.checkChallengeExists(challengeId)
    }
}
