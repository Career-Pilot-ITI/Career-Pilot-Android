package com.iti.careerpilot.ats.presentation.optimizedcv

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.ats.R
import com.iti.careerpilot.core.designsystem.common.ObserveEvent
import com.iti.careerpilot.core.designsystem.components.ButtonVariant
import com.iti.careerpilot.core.designsystem.components.CareerPilotButton
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard

@Composable
fun OptimizedCvRoot(
    workspaceId: Long,
    onBack: () -> Unit,
    openCoinsPaywall: () -> Unit,
    viewModel: OptimizedCvViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val copied = stringResource(R.string.ats_copied)
    val clipboardLabel = stringResource(R.string.optimized_cv)
    LaunchedEffect(workspaceId) { viewModel.loadWorkspace(workspaceId) }
    ObserveEvent(viewModel.effects) { effect ->
        when (effect) {
            is OptimizedCvEffect.CopyText -> {
                val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                manager.setPrimaryClip(ClipData.newPlainText(clipboardLabel, effect.value))
                snackbar.showSnackbar(copied)
            }
            OptimizedCvEffect.OpenCoinsPaywall -> openCoinsPaywall()
        }
    }
    OptimizedCvScreen(state, viewModel::onAction, onBack, snackbar)
}

@Composable
fun OptimizedCvScreen(
    state: OptimizedCvUiState,
    onAction: (OptimizedCvAction) -> Unit,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    androidx.compose.material3.Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onBack) { Text(stringResource(R.string.ats_back)) }
                Text(
                    stringResource(R.string.optimized_cv),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
            }
            if (state.isLoading) {
                androidx.compose.foundation.layout.Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }
            } else {
                OptimizedCvContent(state, onAction)
            }
        }
    }
    if (state.isConfirmationVisible) {
        AlertDialog(
            onDismissRequest = { onAction(OptimizedCvAction.DismissConfirmation) },
            title = { Text(stringResource(R.string.ats_optimize_cv)) },
            text = { Text(stringResource(R.string.ats_paid_operation_confirmation)) },
            confirmButton = {
                TextButton(onClick = { onAction(OptimizedCvAction.ConfirmOptimization) }) {
                    Text(stringResource(R.string.ats_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction(OptimizedCvAction.DismissConfirmation) }) {
                    Text(stringResource(R.string.ats_cancel))
                }
            },
        )
    }
}

@Composable
private fun OptimizedCvContent(
    state: OptimizedCvUiState,
    onAction: (OptimizedCvAction) -> Unit,
) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        state.error?.let { item { Text(it.asString(), color = MaterialTheme.colorScheme.error) } }
        if (state.wasInterrupted) item {
            Text(stringResource(R.string.ats_generation_interrupted), color = MaterialTheme.colorScheme.error)
        }
        if (state.optimizedText.isBlank()) {
            item {
                CareerPilotButton(
                    text = stringResource(R.string.ats_optimize_cv),
                    onClick = { onAction(OptimizedCvAction.RequestOptimization) },
                )
            }
        } else {
            item {
                CareerPilotCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.ats_optimized_text), fontWeight = FontWeight.Bold)
                        Text(state.optimizedText, modifier = Modifier.padding(top = 10.dp))
                    }
                }
            }
            if (state.recommendedTracks.isNotEmpty()) item {
                CareerPilotCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.ats_recommended_tracks), fontWeight = FontWeight.Bold)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.recommendedTracks.forEach { track ->
                                AssistChip(onClick = {}, label = { Text(track) })
                            }
                        }
                    }
                }
            }
            state.coinCost?.let { cost ->
                item { Text(pluralStringResource(R.plurals.ats_coins_used, cost, cost)) }
            }
            item {
                CareerPilotButton(
                    text = stringResource(R.string.ats_copy_optimized_cv),
                    onClick = { onAction(OptimizedCvAction.Copy) },
                )
            }
        }
        if (state.hasInsufficientCoins) item {
            CareerPilotButton(
                text = stringResource(R.string.ats_get_coins),
                onClick = { onAction(OptimizedCvAction.OpenCoins) },
                variant = ButtonVariant.OUTLINE,
            )
        }
    }
}
