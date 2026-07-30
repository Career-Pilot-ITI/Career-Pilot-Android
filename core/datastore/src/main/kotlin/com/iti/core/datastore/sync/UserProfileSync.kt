package com.iti.core.datastore.sync

interface UserProfileSync {
    suspend fun syncWalletBalance(): Int
    suspend fun syncSubscriptionTier(): String
}
