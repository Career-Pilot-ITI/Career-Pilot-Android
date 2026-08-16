package com.iti.careerpilot.core.access.domain.usecase

import com.iti.core.model.FeatureAccess

/**
 * Dispatches a [FeatureAccess] result to the appropriate handler.
 *
 * Use this instead of a raw `when (access)` block in ViewModels to
 * avoid duplicating the gating dispatch logic across feature modules.
 *
 * The [FeatureAccess.Unknown] case is intentionally a no-op; callers should
 * simply not proceed when access state is not yet resolved.
 */
suspend fun FeatureAccess.handle(
    onGranted: suspend (FeatureAccess.Granted) -> Unit,
    onLocked: suspend (FeatureAccess.Locked) -> Unit,
    onCoinTopUpRequired: suspend (FeatureAccess.CoinTopUpRequired) -> Unit,
    onStale: suspend () -> Unit = {},
) {
    when (this) {
        is FeatureAccess.Granted -> onGranted(this)
        is FeatureAccess.Locked -> onLocked(this)
        is FeatureAccess.CoinTopUpRequired -> onCoinTopUpRequired(this)
        is FeatureAccess.StaleCacheBlocked -> onStale()
        is FeatureAccess.Unknown -> { /* no-op: waiting for state */ }
    }
}
