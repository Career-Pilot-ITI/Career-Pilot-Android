package com.iti.careerpilot.challengedetails.data.remote

import com.iti.careerpilot.challengefirestore.Challenge
import com.iti.careerpilot.challengefirestore.ChallengeFirestoreDataSource
import com.iti.common.error.FirebaseError
import com.iti.common.result.CareerPilotResult
import javax.inject.Inject

class ChallengeDetailsRemoteDataSourceImpl @Inject constructor(
    private val firestoreDataSource: ChallengeFirestoreDataSource
) : ChallengeDetailsRemoteDataSource {

    override suspend fun getChallenge(challengeId: String): CareerPilotResult<Challenge, FirebaseError> {
        return firestoreDataSource.getChallenge(challengeId)
    }
}
