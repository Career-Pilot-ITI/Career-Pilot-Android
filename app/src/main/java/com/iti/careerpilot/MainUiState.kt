package com.iti.careerpilot

import com.iti.core.datastore.settings.domain.models.UserSettings


sealed interface MainUiState {
    data object Loading : MainUiState
    data class Ready(val userSettings: UserSettings) : MainUiState
}
