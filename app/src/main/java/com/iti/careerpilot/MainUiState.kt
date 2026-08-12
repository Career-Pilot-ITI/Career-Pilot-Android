package com.iti.careerpilot

import com.iti.careerpilot.features.settings.domain.models.UserSettings


sealed interface MainUiState {
    data object Loading : MainUiState
    data class Ready(val userSettings: UserSettings) : MainUiState
}
