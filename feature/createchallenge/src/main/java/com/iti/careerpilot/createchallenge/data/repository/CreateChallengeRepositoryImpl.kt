package com.iti.careerpilot.createchallenge.data.repository

import com.iti.careerpilot.createchallenge.data.remote.CreateChallengeRemoteDataSource
import com.iti.careerpilot.createchallenge.domain.repository.CreateChallengeRepository
import com.iti.common.error.FirebaseError
import com.iti.common.error.NetworkError
import com.iti.common.result.CareerPilotResult
import com.iti.core.model.Challenge
import com.iti.core.model.Track
import javax.inject.Inject

class CreateChallengeRepositoryImpl @Inject constructor(
    private val remoteDataSource: CreateChallengeRemoteDataSource
) : CreateChallengeRepository {

    override suspend fun getTracks(): CareerPilotResult<List<Track>, NetworkError> {
        return remoteDataSource.getTracks()
    }

    override suspend fun validateQuestions(questions: List<String>): CareerPilotResult<Boolean, FirebaseError> {
        return remoteDataSource.validateQuestions(questions)
    }

    override suspend fun createChallenge(challenge: Challenge): CareerPilotResult<Unit, FirebaseError> {
        return remoteDataSource.saveChallenge(challenge)
    }
}
