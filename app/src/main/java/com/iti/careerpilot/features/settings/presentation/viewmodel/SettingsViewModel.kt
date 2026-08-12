package com.iti.careerpilot.features.settings.presentation.viewmodel

import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.features.settings.domain.UserSettingsRepo
import com.iti.careerpilot.features.settings.domain.models.UserSettings
import com.iti.careerpilot.features.settings.presentation.action.SettingsAction
import com.iti.careerpilot.features.settings.presentation.state.SettingsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepo: UserSettingsRepo,
) : ViewModel() {

    private val _state: MutableStateFlow<SettingsState> = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    fun onAction(action: SettingsAction) {

        when (action) {
            is SettingsAction.UpdateTheme -> {
                updateSettings {
                    it.copy(theme = action.theme)
                }
            }

            is SettingsAction.LanguageDialogToggle -> {
                _state.update {
                    it.copy(showLanguageDialog = action.open)
                }
            }

            is SettingsAction.ThemeDialogToggle -> {
                _state.update {
                    it.copy(showThemeDialog = action.open)
                }
            }
        }
    }

    private fun updateSettings(
        transform: (UserSettings) -> UserSettings
    ) {
        viewModelScope.launch {
            settingsRepo.updateUserSettings(transform)
        }
    }
}

val LocalSettingsUser = compositionLocalOf { UserSettings() }