package com.iti.careerpilot.settings.presentation.viewmodel

import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.core.access.domain.AccessRepository
import com.iti.careerpilot.settings.R
import com.iti.careerpilot.settings.presentation.action.SettingsIntent
import com.iti.careerpilot.settings.presentation.event.SettingsEffect
import com.iti.careerpilot.settings.presentation.state.SettingsState
import com.iti.core.datastore.settings.domain.UserSettingsRepo
import com.iti.core.datastore.settings.domain.models.LanguageSetting
import com.iti.core.datastore.settings.domain.models.LanguageSetting.ARABIC
import com.iti.core.datastore.settings.domain.models.LanguageSetting.ENGLISH
import com.iti.core.datastore.settings.domain.models.ThemeSetting
import com.iti.core.datastore.settings.domain.models.ThemeSetting.DARK
import com.iti.core.datastore.settings.domain.models.ThemeSetting.FOLLOW_SYSTEM
import com.iti.core.datastore.settings.domain.models.ThemeSetting.LIGHT
import com.iti.core.datastore.settings.domain.models.UserSettings
import com.iti.core.model.Plan
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepo: UserSettingsRepo,
    private val accessRepository: AccessRepository,
) : ViewModel() {

    private val _state: MutableStateFlow<SettingsState> = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private val _events = Channel<SettingsEffect>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        observeAccessState()
    }

    private fun observeAccessState() {
        viewModelScope.launch {
            accessRepository.accessState.collect { access ->
                _state.update {
                    it.copy(
                        planDisplayName = access.plan.displayName(),
                        isMaxPlan = (access.plan == Plan.MAX),
                        coinBalance = access.coinBalance,
                    )
                }
            }
        }
    }

    fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.UpdateTheme -> {
                updateSettings {
                    it.copy(theme = intent.theme)
                }
            }

            is SettingsIntent.LanguageDialogToggle -> {
                _state.update {
                    it.copy(showLanguageDialog = intent.open)
                }
            }

            is SettingsIntent.ThemeDialogToggle -> {
                _state.update {
                    it.copy(showThemeDialog = intent.open)
                }
            }

            SettingsIntent.ManageSubscriptionClicked -> {
                viewModelScope.launch {
                    _events.send(
                        SettingsEffect.NavigateToPaywall(
                            showGetCoins = false,
                            showMySubscription = _state.value.isMaxPlan,
                        )
                    )
                }
            }

            SettingsIntent.CoinsClicked -> {
                viewModelScope.launch {
                    _events.send(
                        SettingsEffect.NavigateToPaywall(
                            showGetCoins = true,
                            showMySubscription = false,
                        )
                    )
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