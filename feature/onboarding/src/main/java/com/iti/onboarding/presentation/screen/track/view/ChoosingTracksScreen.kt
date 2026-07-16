package com.iti.onboarding.presentation.screen.track.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.iti.onboarding.presentation.screen.track.state.ChoosingTracksEffects
import com.iti.onboarding.presentation.screen.track.state.ChoosingTracksIntent
import com.iti.onboarding.presentation.screen.track.state.ChoosingTracksUiState
import com.iti.onboarding.presentation.screen.track.viewmodel.ChoosingTracksViewModel


@Composable
fun ChoosingTracksScreen(
    modifier: Modifier = Modifier,
    viewModel: ChoosingTracksViewModel = hiltViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effects ->
                when (effects) {
                    is ChoosingTracksEffects.ShowError -> TODO()
                    ChoosingTracksEffects.NavigateNext -> TODO()
                }
            }
        }
    }

    ChoosingTracksScreenContent(
        state,
        viewModel::onIntent,
        modifier = modifier.safeContentPadding()
    )
}


@Composable
fun ChoosingTracksScreenContent(
    state: State<ChoosingTracksUiState>,
    onIntent: (ChoosingTracksIntent) -> Unit,
    modifier: Modifier = Modifier,
) {

    Column(
        modifier = modifier
    ) {
        Text("Tracks Screen")
    }

}