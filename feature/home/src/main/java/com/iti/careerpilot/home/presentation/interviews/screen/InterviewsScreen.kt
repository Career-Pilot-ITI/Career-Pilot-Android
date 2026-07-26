package com.iti.careerpilot.home.presentation.interviews.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.core.designsystem.CareerPilotPalette
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.common.ObserveEvent
import com.iti.careerpilot.core.designsystem.components.LoadingWave
import com.iti.careerpilot.home.R
import com.iti.careerpilot.home.presentation.components.CareerPilotTopBar
import com.iti.careerpilot.home.presentation.interviews.InterviewsAction
import com.iti.careerpilot.home.presentation.interviews.InterviewsEvent
import com.iti.careerpilot.home.presentation.interviews.InterviewsState
import com.iti.careerpilot.home.presentation.interviews.InterviewsViewModel
import com.iti.careerpilot.home.presentation.interviews.screen.components.SearchField
import com.iti.careerpilot.home.presentation.interviews.screen.components.TrackRow

@Composable
fun InterviewsRoot(
    openReadyToPractice: (trackId: Long, trackName: String) -> Unit,
    onBack: () -> Unit,
    viewModel: InterviewsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveEvent(viewModel.events) { event ->
        when (event) {
            is InterviewsEvent.NavigateToReadyToPractice ->
                openReadyToPractice(event.trackId, event.trackName)
        }
    }

    InterviewsScreen(state = state, onAction = viewModel::onAction, onBack = onBack)
}

@Composable
fun InterviewsScreen(
    state: InterviewsState,
    onAction: (InterviewsAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CareerPilotTopBar(
                title = stringResource(R.string.interviews_title),
                onBack = onBack,
            )
        },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets
            .exclude(WindowInsets.navigationBars),
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = Dimens.SpaceXXL,
                end = Dimens.SpaceXXL,
                top = Dimens.SpaceS,
                bottom = Dimens.SpaceXXXXL,
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceL),
        ) {

            item {
                SearchField(
                    query = state.query,
                    onQueryChange = { onAction(InterviewsAction.QueryChanged(it)) },
                )
            }

            when {
                state.isLoading -> item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.SpaceXXXL),
                        contentAlignment = Alignment.Center,
                    ) {
                        LoadingWave(color = MaterialTheme.colorScheme.primary)
                    }
                }

                state.error != null -> item {
                    Text(
                        text = stringResource(R.string.interviews_retry),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onAction(InterviewsAction.Retry) },
                    )
                }

                else -> {
                    item {
                        Text(
                            text = stringResource(R.string.interviews_available),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }

                    if (state.tracks.isEmpty()) {
                        item {
                            Text(
                                text = stringResource(
                                    if (state.query.isBlank()) R.string.interviews_empty
                                    else R.string.interviews_no_results
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = CareerPilotPalette.gray600,
                            )
                        }
                    } else {
                        items(items = state.tracks, key = { it.id }) { track ->
                            TrackRow(
                                track = track,
                                onClick = {
                                    onAction(InterviewsAction.TrackClicked(track.id, track.name))
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}



