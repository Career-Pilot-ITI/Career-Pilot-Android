package com.iti.onboarding.presentation.screen.track.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.iti.onboarding.R
import com.iti.onboarding.presentation.components.CustomTrackTextField
import com.iti.onboarding.presentation.screen.track.state.ChoosingTracksEffects
import com.iti.onboarding.presentation.screen.track.state.ChoosingTracksIntent
import com.iti.onboarding.presentation.screen.track.state.ChoosingTracksUiState
import com.iti.onboarding.presentation.screen.track.view.components.ActionButton
import com.iti.onboarding.presentation.screen.track.view.components.ChoosingTracksScreenHeader
import com.iti.onboarding.presentation.screen.track.view.components.TracksFlow
import com.iti.onboarding.presentation.screen.track.viewmodel.ChoosingTracksViewModel

@Composable
fun ChoosingTracksScreen(
    onNavigateToNext: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChoosingTracksViewModel = hiltViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effects ->
                when (effects) {
                    is ChoosingTracksEffects.ShowError -> TODO("Show error using snackbar")
                    ChoosingTracksEffects.NavigateNext -> onNavigateToNext()
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


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ChoosingTracksScreenContent(
    state: State<ChoosingTracksUiState>,
    onIntent: (ChoosingTracksIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val enabledActionButton by remember {
        derivedStateOf {
            state.value.selectedTrack?.let { track ->
                track.name != "Other" || state.value.customTrack.isNotBlank()
            } ?: false
        }
    }
    val showCustomTrackTextField by remember {
        derivedStateOf {
            state.value.selectedTrack?.name == "Other"
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(bottom = 20.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        item(key = 1) {
            Column(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ChoosingTracksScreenHeader()

                AnimatedVisibility(state.value.tracks.isNotEmpty()) {
                    TracksFlow(
                        tracks = state.value.tracks,
                        selectedTrack = state.value.selectedTrack,
                        onTrackClick = { track ->
                            onIntent(ChoosingTracksIntent.ToggleTrackSelection(track))
                        },
                        modifier = Modifier.padding(vertical = 24.dp),
                    )
                }

                AnimatedVisibility(visible = showCustomTrackTextField) {
                    Column {
                        CustomTrackTextField(
                            value = state.value.customTrack,
                            label = stringResource(R.string.track_name_label),
                            onValueChange = {
                                onIntent(ChoosingTracksIntent.OnChangeCustomTrackValue(it))
                            }
                        )

                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }

                if (state.value.isLoading || state.value.error != null) {
                    Spacer(Modifier.fillParentMaxHeight(0.35f))

                    AnimatedVisibility(
                        visible = true
                    ) {
                        if (state.value.isLoading) {
                            LoadingIndicator()
                        } else if (state.value.error != null) {
                            Text(state.value.error ?: "")
                        }
                    }
                }
            }
        }

        item(key = 2) {
            ActionButton(
                label = stringResource(R.string.next_button_label),
                onClick = {
                    onIntent(ChoosingTracksIntent.OnNavigateNext)
                },
                enabled = enabledActionButton
            )
        }
    }
}