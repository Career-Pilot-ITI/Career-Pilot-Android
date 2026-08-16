package com.iti.careerpilot.core.access.domain.usecase

import com.iti.core.model.FeatureAccess

/**
 * Dispatches a [FeatureAccess] result to the appropriate handler.
 *
 * Defined as `inline` so that suspend functions can be invoked in lambda
 * bodies even when the call site is not itself inside a coroutine.
 */
inline fun FeatureAccess.handle(
    onGranted: (FeatureAccess.Granted) -> Unit = {},
    onLocked: (FeatureAccess.Locked) -> Unit = {},
    onCoinTopUpRequired: (FeatureAccess.CoinTopUpRequired) -> Unit = {},
    onStale: () -> Unit = {},
    onUnknown: () -> Unit = {},
) {
    when (this) {
        is FeatureAccess.Granted -> onGranted(this)
        is FeatureAccess.Locked -> onLocked(this)
        is FeatureAccess.CoinTopUpRequired -> onCoinTopUpRequired(this)
        is FeatureAccess.StaleCacheBlocked -> onStale()
        is FeatureAccess.Unknown -> onUnknown()
    }
}
