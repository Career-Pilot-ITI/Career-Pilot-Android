package com.iti.careerpilot.core.access.testing

import com.iti.careerpilot.core.access.domain.AccessRepository
import com.iti.core.model.AccessState
import com.iti.core.model.FeatureKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A test fake of [AccessRepository] backed by a [MutableStateFlow].
 *
 * Tests can manipulate [mutableState] directly to control what [accessState]
 * emits, and inspect [refreshCount] / [lastDeductedAmount] to verify
 * interactions.
 */
class FakeAccessRepository(
    initialState: AccessState = AccessState.Free
) : AccessRepository {

    val mutableState = MutableStateFlow(initialState)

    override val accessState: StateFlow<AccessState> = mutableState.asStateFlow()

    /** How many times [refresh] was called. */
    var refreshCount: Int = 0
        private set

    /** Result returned by the next [refresh] call. Defaults to success. */
    var refreshResult: Result<Unit> = Result.success(Unit)

    /** Last amount passed to [deductCoins], or `null` if never called. */
    var lastDeductedAmount: Int? = null
        private set

    override suspend fun refresh(): Result<Unit> {
        refreshCount++
        return refreshResult
    }

    override suspend fun deductCoins(amount: Int) {
        lastDeductedAmount = amount
        val current = mutableState.value
        val updatedBalance = (current.coinBalance - amount).coerceAtLeast(0)
        mutableState.value = current.copy(coinBalance = updatedBalance)
    }

    override fun hasAccess(feature: FeatureKey): Boolean =
        mutableState.value.hasAccess(feature)

    override suspend fun clear() {
        mutableState.value = AccessState.Free
        refreshCount = 0
        lastDeductedAmount = null
    }
}
