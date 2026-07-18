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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.iti.onboarding.presentation.screen.track.state.ChoosingTracksEffects
import com.iti.onboarding.presentation.screen.track.state.ChoosingTracksIntent
import com.iti.onboarding.presentation.screen.track.state.ChoosingTracksUiState
import com.iti.onboarding.presentation.screen.track.view.components.ChoosingTracksScreenHeader
import com.iti.onboarding.presentation.screen.track.view.components.TracksFlow
import com.iti.onboarding.presentation.screen.track.viewmodel.ChoosingTracksViewModel

@Composable
fun ChoosingTracksScreen(
    onNavigateNext: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChoosingTracksViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    is ChoosingTracksEffects.ShowError -> {
                        // TODO: Show the localized message with the base snackbar.
                    }

                    ChoosingTracksEffects.NavigateNext -> onNavigateNext()
                }
            }
        }
    }

    ChoosingTracksScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier.safeContentPadding(),
    )
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class,
)
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
            .fillMaxSize()
            .background(colors.background),
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
                .fillMaxSize()
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            item(key = "tracks_content") {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    ChoosingTracksScreenHeader()

                    AnimatedVisibility(state.tracks.isNotEmpty()) {
                        TracksFlow(
                            tracks = state.tracks,
                            selectedTrack = state.selectedTrack,
                            onTrackClick = { track ->
                                onIntent(
                                    ChoosingTracksIntent.ToggleTrackSelection(track)
                                )
                            },
                            modifier = Modifier.padding(vertical = 24.dp),
                        )
                    }

                    if (state.isLoading) {
                        Spacer(Modifier.fillParentMaxHeight(0.35f))
                        LoadingIndicator(
                            color = colors.primary,
                        )
                    }
                }
            }

            item(key = "bottom_spacing") {
                Spacer(modifier = Modifier.height(120.dp))
            }
        }
    }
}
