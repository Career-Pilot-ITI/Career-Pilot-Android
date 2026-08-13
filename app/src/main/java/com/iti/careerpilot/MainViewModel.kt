package com.iti.careerpilot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.core.datastore.UserTokensRepo
import com.iti.careerpilot.share.PendingSharedText
import com.iti.careerpilot.optimization.PendingCvOptimization
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    userTokensRepo: UserTokensRepo
) : ViewModel() {

    private val _pendingSharedText = MutableStateFlow<String?>(null)
    val pendingSharedText = _pendingSharedText.asStateFlow()
    private val _pendingCvOptimization = MutableStateFlow<PendingCvOptimization?>(null)
    val pendingCvOptimization = _pendingCvOptimization.asStateFlow()

    val isLoggedIn: StateFlow<Boolean?> = userTokensRepo.tokenUpdates
        .map { it.accessToken?.isNotBlank() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    fun acceptSharedText(payload: PendingSharedText?) {
        payload ?: return
        _pendingSharedText.value = payload.value
    }

    fun consumeSharedText() {
        _pendingSharedText.value = null
    }

    fun acceptCvOptimization(payload: PendingCvOptimization?) {
        payload ?: return
        _pendingCvOptimization.value = payload
    }

    fun consumeCvOptimization() {
        _pendingCvOptimization.value = null
    }
}
