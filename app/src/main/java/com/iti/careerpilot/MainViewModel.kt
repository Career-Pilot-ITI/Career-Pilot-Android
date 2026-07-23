package com.iti.careerpilot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.core.datastore.UserTokensRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    userTokensRepo: UserTokensRepo
) : ViewModel() {

    val isLoggedIn: StateFlow<Boolean?> = userTokensRepo.tokenUpdates
        .map { it.accessToken?.isNotBlank() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )
}
