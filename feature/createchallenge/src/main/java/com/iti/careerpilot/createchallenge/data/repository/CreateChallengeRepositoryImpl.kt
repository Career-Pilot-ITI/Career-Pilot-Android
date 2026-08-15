package com.iti.careerpilot.createchallenge.data.repository

import com.iti.careerpilot.createchallenge.data.remote.CreateChallengeRemoteDataSource
import com.iti.careerpilot.createchallenge.domain.repository.CreateChallengeRepository
import javax.inject.Inject

class CreateChallengeRepositoryImpl @Inject constructor(
    private val remoteDataSource: CreateChallengeRemoteDataSource
) : CreateChallengeRepository {
}
