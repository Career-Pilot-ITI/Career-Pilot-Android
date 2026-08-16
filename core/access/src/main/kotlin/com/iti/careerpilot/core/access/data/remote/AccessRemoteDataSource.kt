package com.iti.careerpilot.core.access.data.remote

import com.iti.careerpilot.core.access.data.remote.dto.SubscriptionStatusDto

interface AccessRemoteDataSource {
    suspend fun getSubscriptionStatus(): SubscriptionStatusDto
    suspend fun getWalletBalance(): Int
}
