package com.iti.careerpilot.challengedashboard.data.repository

import com.iti.careerpilot.challengedashboard.data.remote.ChallengeDashboardRemoteDataSource
import com.iti.careerpilot.challengedashboard.domain.repository.ChallengeDashboardRepository
import javax.inject.Inject

class ChallengeDashboardRepositoryImpl @Inject constructor(
    private val remoteDataSource: ChallengeDashboardRemoteDataSource
) : ChallengeDashboardRepository {
}
