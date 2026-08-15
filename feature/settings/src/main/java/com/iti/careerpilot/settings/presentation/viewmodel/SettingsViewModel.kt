package com.iti.careerpilot.settings.presentation.viewmodel

import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.settings.R
import com.iti.core.datastore.settings.domain.UserSettingsRepo
import com.iti.core.datastore.settings.domain.models.UserSettings
import com.iti.careerpilot.settings.presentation.action.SettingsAction
import com.iti.careerpilot.settings.presentation.state.SettingsState
import com.iti.core.datastore.settings.domain.models.LanguageSetting
import com.iti.core.datastore.settings.domain.models.LanguageSetting.ARABIC
import com.iti.core.datastore.settings.domain.models.LanguageSetting.ENGLISH
import com.iti.core.datastore.settings.domain.models.ThemeSetting
import com.iti.core.datastore.settings.domain.models.ThemeSetting.DARK
import com.iti.core.datastore.settings.domain.models.ThemeSetting.FOLLOW_SYSTEM
import com.iti.core.datastore.settings.domain.models.ThemeSetting.LIGHT
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


fun LanguageSetting.getCode(): String {
    return when (this) {
        ENGLISH -> "en"
        ARABIC -> "ar"
    }
}
fun LanguageSetting.getTitleId(): Int {
    return when (this) {
        ENGLISH -> R.string.english
        ARABIC -> R.string.arabic
    }
}


fun ThemeSetting.getTitleId(): Int {
    return when (this) {
        LIGHT -> R.string.light
        DARK -> R.string.dark
        FOLLOW_SYSTEM -> R.string.follow_system
    }
}