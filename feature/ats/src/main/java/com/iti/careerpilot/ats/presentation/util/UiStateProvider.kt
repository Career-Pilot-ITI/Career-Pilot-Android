package com.iti.careerpilot.ats.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember

typealias UiStateProvider<S> = () -> S

@Composable
internal fun <S, T> rememberUiStateValue(
    stateProvider: UiStateProvider<S>,
    selector: (S) -> T,
): State<T> {
    return remember(stateProvider, selector) {
        derivedStateOf { selector(stateProvider()) }
    }
}

@Composable
internal fun <S> rememberUiStateProvider(state: State<S>): UiStateProvider<S> =
    remember(state) { { state.value } }
