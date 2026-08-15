package com.iti.careerpilot.settings.presentation.event

sealed interface SettingsEvent {
    data class NavigateToPaywall(
        val showGetCoins: Boolean,
        val showMySubscription: Boolean
    ) : SettingsEvent
}
