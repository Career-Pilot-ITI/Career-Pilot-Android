package com.iti.careerpilot.ats.presentation.optimizedcv.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.ats.R
import com.iti.careerpilot.ats.presentation.optimizedcv.state.OptimizedCvAction
import com.iti.careerpilot.ats.presentation.optimizedcv.state.OptimizedCvEffect
import com.iti.careerpilot.ats.presentation.optimizedcv.state.OptimizedCvUiState
import com.iti.careerpilot.ats.presentation.optimizedcv.view.components.OptimizedCvContent
import com.iti.careerpilot.ats.presentation.optimizedcv.viewmodel.OptimizedCvViewModel
import com.iti.careerpilot.core.designsystem.common.ObserveEvent
import com.iti.common.snackbar.CareerPilotSnackbarController
import com.iti.common.util.UIText

@Composable
fun OptimizedCvRoot(
    workspaceId: Long,
    onBack: () -> Unit,
    openCoinsPaywall: () -> Unit,
    viewModel: OptimizedCvViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboardLabel = stringResource(R.string.optimized_cv)

    LaunchedEffect(workspaceId) {
        viewModel.loadWorkspace(workspaceId)
    }

    ObserveEvent(viewModel.effects) { effect ->
        when (effect) {
            is OptimizedCvEffect.CopyText -> {
                val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                manager.setPrimaryClip(ClipData.newPlainText(clipboardLabel, effect.value))
                CareerPilotSnackbarController.show(
                    UIText.StringResource(R.string.ats_copied),
                )
            }
            OptimizedCvEffect.OpenCoinsPaywall -> openCoinsPaywall()
        }
    }

    OptimizedCvScreen(
        state = state,
        onAction = viewModel::onAction,
        onBack = onBack,
    )
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun OptimizedCvScreen(
    state: OptimizedCvUiState,
    onAction: (OptimizedCvAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onBack) {
                Text(stringResource(R.string.ats_back))
            }
            Text(
                text = stringResource(R.string.optimized_cv),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
        }
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularWavyProgressIndicator()
            }
        } else {
            OptimizedCvContent(
                state = state,
                onAction = onAction,
                modifier = Modifier.fillMaxSize(),
            )
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
