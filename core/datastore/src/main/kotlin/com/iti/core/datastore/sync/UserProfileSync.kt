package com.iti.core.datastore.sync

interface UserProfileSync {
    suspend fun syncWalletBalance()
    suspend fun syncSubscriptionTier()
}
