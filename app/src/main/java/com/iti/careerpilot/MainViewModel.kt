package com.iti.careerpilot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.core.datastore.CareerPilotPreferencesDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    datastore: CareerPilotPreferencesDataSource
) : ViewModel() {

    val hasCompletedOnboarding: StateFlow<Boolean?> = datastore.hasCompletedOnboarding
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val isLoggedIn: StateFlow<Boolean?> = datastore.token
        .map { !it.isNullOrEmpty() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )
}
