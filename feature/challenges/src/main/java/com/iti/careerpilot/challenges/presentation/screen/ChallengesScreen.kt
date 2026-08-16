package com.iti.careerpilot.challenges.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.challenges.R
import com.iti.careerpilot.challenges.presentation.action.ChallengesAction
import com.iti.careerpilot.challenges.presentation.event.ChallengesEvent
import com.iti.careerpilot.challenges.presentation.state.ChallengesState
import com.iti.careerpilot.challenges.presentation.viewmodel.ChallengesViewModel
import com.iti.careerpilot.core.designsystem.common.GradientIcon
import com.iti.careerpilot.core.designsystem.common.ObserveEvent
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
import com.iti.careerpilot.core.designsystem.components.LoadingWave
import com.iti.core.model.Challenge
import com.iti.core.model.getTitleRes


@Composable
fun ChallengesScreenRoot(
    openCreateChallenge: () -> Unit,
    openChallengeDashboard: () -> Unit,
    openChallengeDetails: (String) -> Unit,
    viewModel: ChallengesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onAction(ChallengesAction.Initial)
    }

    ObserveEvent(viewModel.events) { event ->
        when (event) {
            ChallengesEvent.NavigateToCreateChallenge -> openCreateChallenge()
            ChallengesEvent.NavigateToChallengeDashboard -> openChallengeDashboard()
            is ChallengesEvent.NavigateToChallengeDetails -> openChallengeDetails(event.challengeId)
        }
    }

    ChallengesScreenContent(
        state = state,
        onAction = viewModel::onAction
    )

    if (state.isPrivateCodeDialogOpen) {
        PrivateCodeDialog(
            code = state.privateCode,
            onCodeChange = { viewModel.onAction(ChallengesAction.OnPrivateCodeChange(it)) },
            onDismiss = { viewModel.onAction(ChallengesAction.TogglePrivateCodeDialog) },
            onSubmit = { viewModel.onAction(ChallengesAction.SubmitPrivateCode) }
        )
    }
}


@Composable
fun ChallengesScreenContent(
    state: ChallengesState,
    onAction: (ChallengesAction) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.challenges_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { onAction(ChallengesAction.ChallengeDashboardClicked) }) {
                        Icon(
                            Icons.Default.Dashboard,
                            contentDescription = stringResource(R.string.dashboard)
                        )
                    }
                    IconButton(onClick = { onAction(ChallengesAction.CreateChallengeClicked) }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(R.string.create)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                windowInsets = TopAppBarDefaults.windowInsets.exclude(WindowInsets.statusBars)
            )
        },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(WindowInsets.navigationBars),
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { onAction(ChallengesAction.OnSearchQueryChange(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.challenges_search_placeholder)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = MaterialTheme.shapes.medium,
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )

            // Private Code Button
            Card(
                onClick = { onAction(ChallengesAction.TogglePrivateCodeDialog) },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                        alpha = 0.4f
                    )
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.challenges_enter_code),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Public Challenges List
            if (state.isLoading && state.publicChallenges.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    LoadingWave(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                val filtered = state.filteredChallenges
                if (filtered.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            stringResource(R.string.challenges_empty_list),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(filtered, key = { it.id }) { challenge ->
                            ChallengeItem(
                                challenge = challenge,
                                onClick = { onAction(ChallengesAction.OnChallengeClicked(challenge.id)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChallengeItem(
    challenge: Challenge,
    onClick: () -> Unit
) {
    CareerPilotCard(
        modifier = Modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = challenge.trackName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = stringResource(challenge.seniorityLevel.getTitleRes()),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = stringResource(R.string.created_by, challenge.creatorName),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.questions, challenge.questions.size),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "•",
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = stringResource(challenge.type.getTitleRes()),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun PrivateCodeDialog(
    code: String,
    onCodeChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = false)
    ) {
        OutlinedCard(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(24.dp)
            ) {
                GradientIcon(
                    icon = Icons.Default.Lock,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = stringResource(R.string.challenges_private_dialog_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.challenges_private_dialog_text),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )

                OutlinedTextField(
                    value = code,
                    onValueChange = onCodeChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.challenges_private_dialog_hint)) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            stringResource(R.string.challenges_private_dialog_cancel),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = onSubmit,
                        modifier = Modifier.weight(1f),
                        enabled = code.isNotBlank()
                    ) {
                        Text(
                            stringResource(R.string.challenges_private_dialog_submit),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
