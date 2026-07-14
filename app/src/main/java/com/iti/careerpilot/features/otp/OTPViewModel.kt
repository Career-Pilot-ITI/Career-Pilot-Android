package com.iti.careerpilot.features.otp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class OTPViewModel @Inject constructor() : ViewModel() {

    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(OTPState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                /** Load initial data here **/
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = OTPState()
        )

    fun onAction(action: OTPAction) {
        when (action) {
            else -> TODO("Handle actions")
        }
    }

}