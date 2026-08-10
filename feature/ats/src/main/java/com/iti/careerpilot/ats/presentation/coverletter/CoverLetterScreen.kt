package com.iti.careerpilot.ats.presentation.coverletter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.ats.R
import com.iti.careerpilot.ats.presentation.util.EmailDraft
import com.iti.careerpilot.core.designsystem.common.ObserveEvent
import com.iti.careerpilot.core.designsystem.components.ButtonVariant
import com.iti.careerpilot.core.designsystem.components.CareerPilotButton
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard

@Composable
fun CoverLetterRoot(
    workspaceId: Long,
    onBack: () -> Unit,
    openCoinsPaywall: () -> Unit,
    viewModel: CoverLetterViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val copiedMessage = stringResource(R.string.ats_copied)
    val missingEmailClient = stringResource(R.string.ats_no_email_client)

    LaunchedEffect(workspaceId) { viewModel.loadWorkspace(workspaceId) }
    ObserveEvent(viewModel.effects) { effect ->
        when (effect) {
            is CoverLetterEffect.CopyText -> {
                copyText(context, effect.value)
                snackbarHostState.showSnackbar(copiedMessage)
            }
            is CoverLetterEffect.ComposeEmail -> if (!composeEmail(context, effect.draft)) {
                snackbarHostState.showSnackbar(missingEmailClient)
            }
            CoverLetterEffect.OpenCoinsPaywall -> openCoinsPaywall()
        }
    }
    CoverLetterScreen(state, viewModel::onAction, onBack, snackbarHostState)
}

@Composable
fun CoverLetterScreen(
    state: CoverLetterUiState,
    onAction: (CoverLetterAction) -> Unit,
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
                    stringResource(R.string.cover_letter),
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
                CoverLetterContent(state, onAction)
            }
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

@Composable
private fun CoverLetterContent(
    state: CoverLetterUiState,
    onAction: (CoverLetterAction) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        state.error?.let { error -> item { Text(error.asString(), color = MaterialTheme.colorScheme.error) } }
        if (state.wasInterrupted) item {
            Text(stringResource(R.string.ats_generation_interrupted), color = MaterialTheme.colorScheme.error)
        }
        if (state.editedValue.isBlank()) {
            item {
                CareerPilotButton(
                    text = stringResource(R.string.ats_generate_cover_letter),
                    onClick = { onAction(CoverLetterAction.RequestGeneration) },
                )
            }
        } else {
            item {
                CareerPilotCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                stringResource(R.string.cover_letter),
                                modifier = Modifier.weight(1f),
                                fontWeight = FontWeight.Bold,
                            )
                            TextButton(onClick = { onAction(CoverLetterAction.ToggleEditing) }) {
                                Text(
                                    stringResource(
                                        if (state.isEditing) R.string.ats_done else R.string.ats_edit,
                                    ),
                                )
                            }
                            TextButton(onClick = { onAction(CoverLetterAction.Copy) }) {
                                Text(stringResource(R.string.ats_copy))
                            }
                        }
                        if (state.isEditing) {
                            OutlinedTextField(
                                value = state.editedValue,
                                onValueChange = { onAction(CoverLetterAction.EditedValueChanged(it)) },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 12,
                            )
                        } else {
                            Text(state.editedValue, style = MaterialTheme.typography.bodyLarge)
                        }
                        val contacts = listOf(state.contactName, state.contactEmail, state.contactPhone)
                            .filter(String::isNotBlank)
                        if (contacts.isNotEmpty()) {
                            FlowRow(
                                modifier = Modifier.padding(top = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                contacts.forEach { AssistChip(onClick = {}, label = { Text(it) }) }
                            }
                        }
                    }
                }
            }
            state.approachTips?.takeIf(String::isNotBlank)?.let { tips ->
                item {
                    CareerPilotCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(stringResource(R.string.ats_approach_tips), fontWeight = FontWeight.Bold)
                            Text(
                                tips,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                    }
                }
            }
            state.coinCost?.let { cost -> item { Text(stringResource(R.string.ats_coins_used, cost)) } }
            item {
                CareerPilotButton(
                    text = stringResource(R.string.ats_send_email),
                    onClick = { onAction(CoverLetterAction.Email) },
                )
            }
        }
        if (state.hasInsufficientCoins) item {
            CareerPilotButton(
                text = stringResource(R.string.ats_get_coins),
                onClick = { onAction(CoverLetterAction.OpenCoins) },
                variant = ButtonVariant.OUTLINE,
            )
        }
    }
}

private fun copyText(context: Context, value: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(context.getString(R.string.cover_letter), value))
}

private fun composeEmail(context: Context, draft: EmailDraft): Boolean {
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
