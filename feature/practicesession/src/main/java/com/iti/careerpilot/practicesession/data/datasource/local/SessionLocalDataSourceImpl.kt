package com.iti.careerpilot.practicesession.data.datasource.local

import com.iti.careerpilot.practicesession.domain.repo.SessionLocalDataSource
import com.iti.core.datastore.repo.UserProfileRepo
import javax.inject.Inject

class SessionLocalDataSourceImpl @Inject constructor(
    private val userProfileRepo: UserProfileRepo,
): SessionLocalDataSource {



}