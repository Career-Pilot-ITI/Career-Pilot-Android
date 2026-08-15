package com.iti.careerpilot.challenges.data.repository

import com.iti.careerpilot.challenges.data.remote.ChallengesRemoteDataSource
import com.iti.careerpilot.challenges.domain.repository.ChallengesRepository
import javax.inject.Inject

class ChallengesRepositoryImpl @Inject constructor(
    private val remoteDataSource: ChallengesRemoteDataSource
) : ChallengesRepository {
}
