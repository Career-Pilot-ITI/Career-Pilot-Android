package com.iti.careerpilot.ats.presentation.coverletter.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.ats.R
import com.iti.careerpilot.ats.presentation.coverletter.state.CoverLetterAction
import com.iti.careerpilot.ats.presentation.coverletter.state.CoverLetterEffect
import com.iti.careerpilot.ats.presentation.coverletter.state.CoverLetterUiState
import com.iti.careerpilot.ats.presentation.coverletter.view.components.CoverLetterContent
import com.iti.careerpilot.ats.presentation.coverletter.viewmodel.CoverLetterViewModel
import com.iti.careerpilot.ats.presentation.util.EmailDraft
import com.iti.careerpilot.core.designsystem.common.ObserveEvent
import com.iti.common.snackbar.CareerPilotSnackbarController
import com.iti.common.util.UIText

@Composable
fun CoverLetterRoot(
    workspaceId: Long,
    onBack: () -> Unit,
    openCoinsPaywall: () -> Unit,
    viewModel: CoverLetterViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboardLabel = stringResource(R.string.cover_letter)

    LaunchedEffect(workspaceId) {
        viewModel.loadWorkspace(workspaceId)
    }

    ObserveEvent(viewModel.effects) { effect ->
        when (effect) {
            is CoverLetterEffect.CopyText -> {
                copyText(context, clipboardLabel, effect.value)
                CareerPilotSnackbarController.show(
                    UIText.StringResource(R.string.ats_copied),
                )
            }
            is CoverLetterEffect.ComposeEmail -> if (!composeEmail(context, effect.draft)) {
                CareerPilotSnackbarController.show(
                    UIText.StringResource(R.string.ats_no_email_client),
                )
            }
            CoverLetterEffect.OpenCoinsPaywall -> openCoinsPaywall()
        }
    }

    CoverLetterScreen(
        state = state,
        onAction = viewModel::onAction,
        onBack = onBack,
    )
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun CoverLetterScreen(
    state: CoverLetterUiState,
    onAction: (CoverLetterAction) -> Unit,
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
                text = stringResource(R.string.cover_letter),
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
            CoverLetterContent(
                state = state,
                onAction = onAction,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    if (state.isConfirmationVisible) {
        AlertDialog(
            onDismissRequest = { onAction(CoverLetterAction.DismissConfirmation) },
            title = { Text(stringResource(R.string.ats_generate_cover_letter)) },
            text = { Text(stringResource(R.string.ats_paid_operation_confirmation)) },
            confirmButton = {
                TextButton(onClick = { onAction(CoverLetterAction.ConfirmGeneration) }) {
                    Text(stringResource(R.string.ats_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction(CoverLetterAction.DismissConfirmation) }) {
                    Text(stringResource(R.string.ats_cancel))
                }
            },
        )
    }
}

private fun copyText(
    context: Context,
    label: String,
    value: String,
) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
}

private fun composeEmail(
    context: Context,
    draft: EmailDraft,
): Boolean {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = "mailto:".toUri()
        putExtra(Intent.EXTRA_SUBJECT, draft.subject.asString(context))
        putExtra(Intent.EXTRA_TEXT, draft.body)
    }
    return if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
        true
    } else {
        false
    }
}
