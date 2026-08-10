package com.iti.careerpilot.ats.presentation.entry

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.ats.R
import com.iti.careerpilot.core.designsystem.components.CareerPilotButton
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
import kotlinx.coroutines.delay
import com.iti.careerpilot.core.designsystem.common.ObserveEvent

@Composable
fun AtsEntryRoot(
    initialSharedText: String?,
    onSharedTextConsumed: () -> Unit,
    onWorkspaceImported: (Long) -> Unit,
    viewModel: AtsEntryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.onAction(AtsEntryAction.PdfSelected(it.toString())) }
    }
    val messageState = remember { androidx.compose.material3.SnackbarHostState() }

    LaunchedEffect(initialSharedText) {
        initialSharedText?.let {
            viewModel.onAction(AtsEntryAction.SharedTextReceived(it))
            onSharedTextConsumed()
        }
    }

    ObserveEvent(viewModel.effects) { effect ->
        when (effect) {
            AtsEntryEffect.OpenPdfPicker -> launcher.launch(PDF_MIME_TYPE)
            is AtsEntryEffect.NavigateToJobDetails -> onWorkspaceImported(effect.workspaceId)
            is AtsEntryEffect.ShowMessage -> messageState.showSnackbar(effect.message.asString(context))
        }
    }

    AtsEntryScreen(
        state = state,
        onAction = viewModel::onAction,
        snackbarHostState = messageState,
    )
}

@Composable
fun AtsEntryScreen(
    state: AtsEntryUiState,
    onAction: (AtsEntryAction) -> Unit,
    snackbarHostState: androidx.compose.material3.SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val jobUrlDescription = stringResource(R.string.ats_job_posting_link)
    val importMessages = listOf(
        stringResource(R.string.ats_importing_job),
        stringResource(R.string.ats_importing_details),
        stringResource(R.string.ats_preparing_workspace),
    )
    var importMessageIndex by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(state.isImporting) {
        if (!state.isImporting) {
            importMessageIndex = 0
            return@LaunchedEffect
        }
        while (true) {
            delay(1_600)
            importMessageIndex = (importMessageIndex + 1) % importMessages.size
        }
    }

    androidx.compose.material3.Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { androidx.compose.material3.SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(top = 24.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                item {
                    Text(
                        text = stringResource(R.string.ats_job_match).uppercase(),
                        color = MaterialTheme.colorScheme.tertiary,
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        text = stringResource(R.string.ats_compare_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                    Text(
                        text = stringResource(R.string.ats_compare_subtitle),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
                item {
                    Text(
                        text = stringResource(R.string.ats_job_posting_link),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    OutlinedTextField(
                        value = state.jobUrl,
                        onValueChange = { onAction(AtsEntryAction.JobUrlChanged(it)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .semantics { contentDescription = jobUrlDescription },
                        placeholder = { Text(stringResource(R.string.ats_job_url_hint)) },
                        singleLine = true,
                        enabled = !state.isBusy,
                        isError = state.jobUrl.isNotBlank() && !state.isUrlValid,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    )
                    Text(
                        text = stringResource(R.string.ats_supported_job_sites),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
                item {
                    Text(
                        text = stringResource(R.string.ats_cv_on_file),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Spacer(Modifier.height(8.dp))
                    if (state.hasSynchronizedCv) {
                        CurrentCvCard(state = state, onReplace = {
                            onAction(AtsEntryAction.SelectCvClicked)
                        })
                    } else {
                        MissingCvCard(
                            enabled = !state.isBusy,
                            onUpload = { onAction(AtsEntryAction.SelectCvClicked) },
                        )
                    }
                }
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f),
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f)),
                    ) {
                        Text(
                            text = stringResource(R.string.ats_share_tip),
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
            }
            CareerPilotButton(
                text = if (state.isImporting) importMessages[importMessageIndex]
                else stringResource(R.string.ats_compare_now),
                onClick = { onAction(AtsEntryAction.CompareClicked) },
                enabled = state.canCompare,
                modifier = Modifier.padding(bottom = 16.dp),
            )
        }
    }
}

@Composable
private fun CurrentCvCard(
    state: AtsEntryUiState,
    onReplace: () -> Unit,
) {
    CareerPilotCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = state.cvFileName.ifBlank { stringResource(R.string.ats_current_cv) },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                if (state.cvSizeBytes > 0L) {
                    Text(
                        text = stringResource(R.string.ats_cv_size, state.cvSizeBytes / 1024L),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                if (state.isUploadingCv) {
                    CircularProgressIndicator(
                        progress = { state.uploadProgress / 100f },
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
            androidx.compose.material3.TextButton(
                onClick = onReplace,
                enabled = !state.isBusy,
            ) { Text(stringResource(R.string.ats_replace_cv)) }
        }
    }
}

@Composable
private fun MissingCvCard(
    enabled: Boolean,
    onUpload: () -> Unit,
) {
    Card(
        onClick = onUpload,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 28.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.ats_upload_your_cv),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.ats_pdf_limit),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

private const val PDF_MIME_TYPE = "application/pdf"
