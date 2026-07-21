package com.iti.careerpilot.practicesession.data.datasource

import com.iti.careerpilot.practicesession.domain.repo.SessionLocalDataSource
import com.iti.careerpilot.practicesession.domain.repo.SessionRemoteDataSource
import com.iti.careerpilot.practicesession.domain.repo.SessionRepo
import javax.inject.Inject

class SessionRepoImpl @Inject constructor(
    private val localDataSource: SessionLocalDataSource,
    private val remoteDataSource: SessionRemoteDataSource
): SessionRepo {
}