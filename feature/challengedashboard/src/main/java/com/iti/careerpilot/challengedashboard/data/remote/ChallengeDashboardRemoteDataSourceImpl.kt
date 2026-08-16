package com.iti.careerpilot.challengedashboard.data.remote

import com.iti.careerpilot.challengefirestore.Challenge
import com.iti.careerpilot.challengefirestore.ChallengeFirestoreDataSource
import com.iti.careerpilot.challengefirestore.ChallengeSession
import com.iti.careerpilot.challengefirestore.ChallengeVisibility
import com.iti.common.error.FirebaseError
import com.iti.common.result.CareerPilotResult
import javax.inject.Inject

class ChallengeDashboardRemoteDataSourceImpl @Inject constructor(
    private val firestoreDataSource: ChallengeFirestoreDataSource
) : ChallengeDashboardRemoteDataSource {

    override suspend fun getCreatedChallenges(creatorId: Long): CareerPilotResult<List<Challenge>, FirebaseError> {
        return firestoreDataSource.getCreatedChallenges(creatorId)
    }

    override suspend fun getTakenChallenges(participantId: Long): CareerPilotResult<List<ChallengeSession>, FirebaseError> {
        return firestoreDataSource.getTakenChallenges(participantId)
    }

    override suspend fun getChallengeSessions(challengeId: String): CareerPilotResult<List<ChallengeSession>, FirebaseError> {
        return firestoreDataSource.getChallengeSessions(challengeId)
    }

    override suspend fun deleteChallenge(challengeId: String, visibility: ChallengeVisibility): CareerPilotResult<Unit, FirebaseError> {
        return firestoreDataSource.deleteChallenge(challengeId, visibility)
    }
}
