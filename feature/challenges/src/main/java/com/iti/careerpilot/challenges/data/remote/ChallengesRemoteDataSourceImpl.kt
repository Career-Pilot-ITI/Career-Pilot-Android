package com.iti.careerpilot.challenges.data.remote

import com.iti.careerpilot.challengefirestore.Challenge
import com.iti.careerpilot.challengefirestore.ChallengeFirestoreDataSource
import com.iti.common.error.FirebaseError
import com.iti.common.result.CareerPilotResult
import javax.inject.Inject

class ChallengesRemoteDataSourceImpl @Inject constructor(
    private val firestoreDataSource: ChallengeFirestoreDataSource
) : ChallengesRemoteDataSource {

    override suspend fun getPublicChallenges(): CareerPilotResult<List<Challenge>, FirebaseError> {
        return firestoreDataSource.getPublicChallenges()
    }

    override suspend fun checkChallengeExists(challengeId: String): CareerPilotResult<Boolean, FirebaseError> {
        return firestoreDataSource.checkChallengeExists(challengeId)
    }
}
