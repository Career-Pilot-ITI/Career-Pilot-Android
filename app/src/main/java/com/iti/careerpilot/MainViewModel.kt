package com.iti.careerpilot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.core.datastore.UserTokensRepo
import com.iti.core.datastore.settings.domain.UserSettingsRepo
import com.iti.core.datastore.settings.domain.models.LanguageSetting
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    userTokensRepo: UserTokensRepo,
    private val userSettingsRepo: UserSettingsRepo
) : ViewModel() {

    val isLoggedIn: StateFlow<Boolean?> = userTokensRepo.tokenUpdates
        .map { it.accessToken?.isNotBlank() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )
    val mainUiState: Flow<MainUiState> = userSettingsRepo.settingsFlow
        .map {
            MainUiState.Ready(it)
        }

    fun saveLanguageSettings(languageSetting: LanguageSetting) {
        viewModelScope.launch {
            userSettingsRepo.updateUserSettings {
                it.copy(language = languageSetting)
            }
        }
    }
}
