package com.iti.onboarding.presentation.screen.track.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.core.designsystem.common.ObserveEvent
import com.iti.careerpilot.core.designsystem.components.LoadingDialog
import com.iti.common.snackbar.CareerPilotSnackbarController
import com.iti.onboarding.R
import com.iti.onboarding.presentation.screen.track.state.ChoosingTracksEffects
import com.iti.onboarding.presentation.screen.track.state.ChoosingTracksIntent
import com.iti.onboarding.presentation.screen.track.state.ChoosingTracksUiState
import com.iti.onboarding.presentation.screen.track.view.components.ChoosingTracksScreenHeader
import com.iti.onboarding.presentation.screen.track.view.components.TracksFlow
import com.iti.onboarding.presentation.screen.track.viewmodel.ChoosingTracksViewModel

@Composable
fun ChoosingTracksScreen(
    isCurrent: Boolean,
    onNavigateNext: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChoosingTracksViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(isCurrent) {
        if (isCurrent) {
            viewModel.onIntent(ChoosingTracksIntent.FetchTracks)
        }
    }

    ObserveEvent(viewModel.effects) { effect ->
        when (effect) {
            is ChoosingTracksEffects.ShowError -> {
                CareerPilotSnackbarController.show(
                    message = effect.message,
                )
            }

            ChoosingTracksEffects.NavigateNext -> onNavigateNext()
        }
    }

    ChoosingTracksScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
    if (state.isLoading) {
        LoadingDialog(
            title = stringResource(R.string.getting_tracks)
        )
    }
    if (state.isSubmitting) {
        LoadingDialog(
            title = stringResource(R.string.submitting)
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ChoosingTracksScreenContent(
    state: ChoosingTracksUiState,
    onIntent: (ChoosingTracksIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val pullToRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = {
            onIntent(ChoosingTracksIntent.OnRefresh)
        },
        state = pullToRefreshState,
        modifier = modifier
            .fillMaxSize(),
        indicator = {
            PullToRefreshDefaults.LoadingIndicator(
                state = pullToRefreshState,
                isRefreshing = state.isRefreshing,
                modifier = Modifier.align(Alignment.TopCenter),
                color = colors.primary,
                containerColor = colors.surface,
            )
        },
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
        ) {
            item(key= "tracks_header") {
                ChoosingTracksScreenHeader()
            }

            item(key = "tracks_content") {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    AnimatedVisibility(
                        state.tracks.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        TracksFlow(
                            tracks = state.tracks,
                            selectedTrack = state.selectedTrack,
                            onTrackClick = { track ->
                                onIntent(
                                    ChoosingTracksIntent.ToggleTrackSelection(track)
                                )
                            },
                            modifier = Modifier,
                        )
                    }
                }
            }
        }
    }
}
