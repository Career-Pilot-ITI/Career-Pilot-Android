package com.iti.careerpilot.profile.data.datasource.remote

import com.iti.careerpilot.profile.domain.datasource.remote.ProfileRemoteDataSource
import io.ktor.client.HttpClient
import javax.inject.Inject

class ProfileRemoteDataSourceImpl @Inject constructor(
    private val httpClient: HttpClient
): ProfileRemoteDataSource {
}