package com.iti.careerpilot.challengedetails.data.repository

import com.iti.careerpilot.challengedetails.data.remote.ChallengeDetailsRemoteDataSource
import com.iti.careerpilot.challengedetails.domain.repository.ChallengeDetailsRepository
import javax.inject.Inject

class ChallengeDetailsRepositoryImpl @Inject constructor(
    private val remoteDataSource: ChallengeDetailsRemoteDataSource
) : ChallengeDetailsRepository {
}
