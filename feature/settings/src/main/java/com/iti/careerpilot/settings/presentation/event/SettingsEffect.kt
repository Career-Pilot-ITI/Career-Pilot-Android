package com.iti.careerpilot.settings.presentation.event

sealed interface SettingsEffect {
    data class NavigateToPaywall(
        val showGetCoins: Boolean,
        val showMySubscription: Boolean
    ) : SettingsEffect
}
