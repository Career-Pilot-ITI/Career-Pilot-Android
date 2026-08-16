package com.iti.careerpilot.challengedashboard.presentation.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.challengedashboard.R
import com.iti.careerpilot.challengedashboard.presentation.action.ChallengeDashboardAction
import com.iti.careerpilot.challengedashboard.presentation.event.ChallengeDashboardEvent
import com.iti.careerpilot.challengedashboard.presentation.state.ChallengeDashboardState
import com.iti.careerpilot.challengedashboard.presentation.state.DashboardTab
import com.iti.careerpilot.challengedashboard.presentation.viewmodel.ChallengeDashboardViewModel
import com.iti.careerpilot.challengefirestore.Challenge
import com.iti.careerpilot.challengefirestore.ChallengeSession
import com.iti.careerpilot.challengefirestore.ChallengeVisibility
import com.iti.careerpilot.challengefirestore.getTitleRes
import com.iti.careerpilot.core.designsystem.common.ObserveEvent
import com.iti.careerpilot.core.designsystem.components.BackIconButton
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
import com.iti.careerpilot.core.designsystem.components.LoadingDialog
import java.text.SimpleDateFormat
import java.util.Date


@Composable
fun ChallengeDashboardScreenRoot(
    onBack: () -> Unit,
    onEditChallenge: (String) -> Unit,
    onViewChallenge: (String) -> Unit,
    onContinueSession: (String) -> Unit,
    viewModel: ChallengeDashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onAction(ChallengeDashboardAction.Initial)
    }

    ObserveEvent(viewModel.events) { event ->
        when (event) {
            ChallengeDashboardEvent.NavigateBack -> onBack()
            is ChallengeDashboardEvent.NavigateToEditChallenge -> onEditChallenge(event.challengeId)
            is ChallengeDashboardEvent.NavigateToSessionDetails -> onViewChallenge(event.sessionId)
            is ChallengeDashboardEvent.ContinueSession -> onContinueSession(event.sessionId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.challenge_dashboard_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    BackIconButton(onBack = { viewModel.onAction(ChallengeDashboardAction.OnBackClicked) })
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        ChallengeDashboardScreenContent(
            modifier = Modifier.padding(padding),
            state = state,
            onAction = viewModel::onAction
        )
    }

    state.participantSessions?.let { sessions ->
        ParticipantReportsDialog(
            sessions = sessions,
            onDismiss = { viewModel.onAction(ChallengeDashboardAction.OnDismissParticipantReports) },
            onViewSession = { viewModel.onAction(ChallengeDashboardAction.OnTakenChallengeClicked(it)) }
        )
    }
    if (state.isLoading) {
        LoadingDialog()
    }
}

@Composable
fun ChallengeDashboardScreenContent(
    modifier: Modifier = Modifier,
    state: ChallengeDashboardState,
    onAction: (ChallengeDashboardAction) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tabs
        DashboardTabs(
            selectedTab = state.selectedTab,
            onTabSelected = { onAction(ChallengeDashboardAction.OnTabSelected(it)) }
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            if (state.selectedTab == DashboardTab.MY_CHALLENGES) {
                items(state.createdChallenges) { challenge ->
                    CreatedChallengeItem(
                        challenge = challenge,
                        onEdit = { onAction(ChallengeDashboardAction.OnEditChallenge(challenge.id)) },
                        onDelete = { onAction(ChallengeDashboardAction.OnDeleteChallenge(challenge.id, challenge.visibility)) },
                        onViewReports = { onAction(ChallengeDashboardAction.OnViewParticipantReports(challenge.id)) }
                    )
                }
            } else {
                items(state.takenChallenges) { session ->
                    TakenChallengeItem(
                        session = session,
                        onClick = { onAction(ChallengeDashboardAction.OnTakenChallengeClicked(session.sessionId)) }
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardTabs(selectedTab: DashboardTab, onTabSelected: (DashboardTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), MaterialTheme.shapes.large)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        DashboardTab.entries.forEach { tab ->
            val isSelected = selectedTab == tab
            val bgColor by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, label = "tabBg")
            val contentColor by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, label = "tabText")

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(MaterialTheme.shapes.large)
                    .background(bgColor)
                    .clickable { onTabSelected(tab) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(tab.titleRes),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            }
        }
    }
}

@Composable
fun CreatedChallengeItem(
    challenge: Challenge,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onViewReports: () -> Unit
) {
    CareerPilotCard {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(challenge.trackName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(challenge.seniorityLevel.getTitleRes()), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Text("•", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Text(stringResource(R.string.questions_count, challenge.questions.size), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Text("•", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Text(stringResource(challenge.type.getTitleRes()), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    }
                }
                Row {
                    IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.secondary) }
                    IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error) }
                }
            }

            if (challenge.visibility == ChallengeVisibility.PRIVATE && challenge.invitationCode.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.challenge_code_label, challenge.invitationCode),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(challenge.visibility.getTitleRes()), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                Button(onClick = onViewReports, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                    Text(stringResource(R.string.view_reports), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun TakenChallengeItem(session: ChallengeSession, onClick: () -> Unit) {
    val locale = androidx.compose.ui.text.intl.Locale.current.platformLocale
    val date = remember(session.timestamp) {
        SimpleDateFormat("dd MMM yyyy", locale).format(Date(session.timestamp))
    }
    CareerPilotCard(modifier = Modifier.clickable { onClick() }) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(session.challengeTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text("•", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(
                        text = stringResource(R.string.questions_progress, session.answeredCount, session.maxQuestions),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text("•", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(
                        text = if (session.status == "COMPLETED") stringResource(R.string.status_completed) else stringResource(R.string.status_in_progress),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (session.status == "COMPLETED") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                }
            }
            session.overallScore?.let {
                Text("$it%", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
fun ParticipantReportsDialog(
    sessions: List<ChallengeSession>,
    onDismiss: () -> Unit,
    onViewSession: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        CareerPilotCard {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(stringResource(R.string.participant_reports_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (sessions.isEmpty()) {
                    Text(stringResource(R.string.no_participants_message), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                        items(sessions) { session ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onViewSession(session.sessionId) }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(session.participantName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(session.participantEmail, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                        Text("•", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                        Text(stringResource(R.string.questions_progress, session.answeredCount, session.maxQuestions), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                        Text("•", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                        Text(
                                            text = if (session.status == "COMPLETED") stringResource(R.string.status_completed) else stringResource(R.string.status_in_progress),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (session.status == "COMPLETED") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                                session.overallScore?.let { Text("$it%", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) }
                            }
                            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        }
                    }
                }
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text(stringResource(R.string.close))
                }
            }
        }
    }
}
