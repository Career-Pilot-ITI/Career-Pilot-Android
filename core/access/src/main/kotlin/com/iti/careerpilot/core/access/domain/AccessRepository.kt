package com.iti.careerpilot.core.access.domain

import com.iti.core.model.AccessState
import com.iti.core.model.FeatureKey
import kotlinx.coroutines.flow.StateFlow

interface AccessRepository {
    val accessState: StateFlow<AccessState>
    suspend fun refresh(): Result<Unit>
    suspend fun deductCoins(amount: Int)
    fun hasAccess(feature: FeatureKey): Boolean
    suspend fun clear()
}
