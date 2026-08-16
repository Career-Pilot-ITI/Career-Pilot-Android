package com.iti.careerpilot.challengedashboard.data.repository

import com.iti.careerpilot.challengefirestore.Challenge
import com.iti.careerpilot.challengefirestore.ChallengeSession
import com.iti.careerpilot.challengefirestore.ChallengeVisibility
import com.iti.careerpilot.challengedashboard.data.remote.ChallengeDashboardRemoteDataSource
import com.iti.careerpilot.challengedashboard.domain.repository.ChallengeDashboardRepository
import com.iti.common.error.FirebaseError
import com.iti.common.result.CareerPilotResult
import javax.inject.Inject

class ChallengeDashboardRepositoryImpl @Inject constructor(
    private val remoteDataSource: ChallengeDashboardRemoteDataSource
) : ChallengeDashboardRepository {

    override suspend fun getCreatedChallenges(creatorId: Long): CareerPilotResult<List<Challenge>, FirebaseError> {
        return remoteDataSource.getCreatedChallenges(creatorId)
    }

    override suspend fun getTakenChallenges(participantId: Long): CareerPilotResult<List<ChallengeSession>, FirebaseError> {
        return remoteDataSource.getTakenChallenges(participantId)
    }

    override suspend fun getChallengeSessions(challengeId: String): CareerPilotResult<List<ChallengeSession>, FirebaseError> {
        return remoteDataSource.getChallengeSessions(challengeId)
    }

    override suspend fun deleteChallenge(challengeId: String, visibility: ChallengeVisibility): CareerPilotResult<Unit, FirebaseError> {
        return remoteDataSource.deleteChallenge(challengeId, visibility)
    }
}
