package com.iti.careerpilot.createchallenge.presentation.screen

import android.content.ClipData
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.core.designsystem.common.GradientIcon
import com.iti.careerpilot.core.designsystem.common.ObserveEvent
import com.iti.careerpilot.core.designsystem.components.BackIconButton
import com.iti.careerpilot.core.designsystem.components.CareerPilotButton
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
import com.iti.careerpilot.core.designsystem.components.LoadingDialog
import com.iti.careerpilot.core.designsystem.components.LoadingWave
import com.iti.careerpilot.createchallenge.R
import com.iti.careerpilot.createchallenge.domain.models.ChallengeType
import com.iti.careerpilot.createchallenge.domain.models.ChallengeVisibility
import com.iti.careerpilot.createchallenge.domain.models.SeniorityLevel
import com.iti.careerpilot.createchallenge.presentation.action.CreateChallengeAction
import com.iti.careerpilot.createchallenge.presentation.event.CreateChallengeEvent
import com.iti.careerpilot.createchallenge.presentation.state.CreateChallengeState
import com.iti.careerpilot.createchallenge.presentation.viewmodel.CreateChallengeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChallengeScreenRoot(
    onBack: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    viewModel: CreateChallengeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onAction(CreateChallengeAction.Initialize)
    }

    ObserveEvent(viewModel.events) { event ->
        when (event) {
            CreateChallengeEvent.NavigateBack -> onBack()
            CreateChallengeEvent.NavigateToDashboard -> onNavigateToDashboard()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.create_challenge_title)) },
                navigationIcon = {
                    BackIconButton(onBack = { viewModel.onAction(CreateChallengeAction.OnBackClicked) })
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        CreateChallengeScreenContent(
            modifier = Modifier.padding(padding),
            state = state,
            onAction = viewModel::onAction
        )
    }

    if (state.isSubmitting) {
        LoadingDialog(title = stringResource(R.string.creating_challenge))
    }

    state.invitationCode?.let { code ->
        SuccessDialog(
            code = code,
            onDismiss = { viewModel.onAction(CreateChallengeAction.OnDismissSuccess) }
        )
    }

    if (state.questionToDeleteIndex != null) {
        ConfirmationDialog(
            title = stringResource(R.string.create_challenge_delete_confirm_title),
            text = stringResource(R.string.create_challenge_delete_confirm_text),
            icon = Icons.Default.Warning,
            cancel = stringResource(R.string.create_challenge_delete_confirm_cancel),
            confirm = stringResource(R.string.create_challenge_delete_confirm_delete),
            onDismiss = { viewModel.onAction(CreateChallengeAction.OnDismissDeleteConfirmation) },
            onConfirm = { viewModel.onAction(CreateChallengeAction.OnConfirmDeleteQuestion) }
        )
    }
}

@Composable
fun ConfirmationDialog(
    title: String,
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    cancel: String,
    confirm: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = false)
    ) {
        OutlinedCard(
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(24.dp)
            ) {
                GradientIcon(
                    icon = icon,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(cancel, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(confirm, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SuccessDialog(
    code: String,
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboard.current
    val scope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = false)
    ) {
        OutlinedCard(
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(24.dp)
            ) {
                GradientIcon(
                    icon = Icons.Default.Check,
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = stringResource(R.string.create_challenge_success_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .clickable {
                            scope.launch {
                                val clipData = ClipData.newPlainText("code", code)
                                val clipEntry = ClipEntry(clipData)
                                clipboardManager.setClipEntry(clipEntry)
                            }
                        }
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.create_challenge_code_label),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = code,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                    Text(
                        text = stringResource(R.string.create_challenge_copy_code),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                CareerPilotButton(
                    text = stringResource(R.string.create_challenge_done),
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun CreateChallengeScreenContent(
    modifier: Modifier = Modifier,
    state: CreateChallengeState,
    onAction: (CreateChallengeAction) -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp, start = 20.dp, end = 20.dp)
    ) {
        item {
            Text(
                stringResource(R.string.create_challenge_general_info),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            CareerPilotCard {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Track Selection
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            stringResource(R.string.create_challenge_track),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (state.isLoadingTracks) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp), contentAlignment = Alignment.Center
                            ) {
                                LoadingWave(color = MaterialTheme.colorScheme.primary)
                            }
                        } else {
                            var expanded by remember { mutableStateOf(false) }
                            Box {
                                Card(
                                    onClick = { expanded = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = MaterialTheme.shapes.medium,
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                            alpha = 0.4f
                                        )
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = state.selectedTrack?.name
                                                ?: stringResource(R.string.create_challenge_select_track),
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Icon(
                                            Icons.Default.KeyboardArrowDown,
                                            contentDescription = null
                                        )
                                    }
                                }
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                    modifier = Modifier.fillMaxWidth(0.8f)
                                ) {
                                    state.tracks.forEach { track ->
                                        DropdownMenuItem(
                                            text = { Text(track.name) },
                                            onClick = {
                                                onAction(CreateChallengeAction.OnTrackSelected(track))
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Seniority Level
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            stringResource(R.string.create_challenge_seniority),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(SeniorityLevel.entries) { level ->
                                val isSelected = state.seniorityLevel == level
                                val containerColor by animateColorAsState(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(
                                        alpha = 0.5f
                                    ),
                                    label = "chipBg"
                                )
                                val contentColor by animateColorAsState(
                                    if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    label = "chipText"
                                )

                                Surface(
                                    onClick = {
                                        onAction(
                                            CreateChallengeAction.OnSeniorityLevelChanged(
                                                level
                                            )
                                        )
                                    },
                                    shape = MaterialTheme.shapes.medium,
                                    color = containerColor,
                                    contentColor = contentColor,
                                    border = if (isSelected) null else BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier.padding(
                                            horizontal = 16.dp,
                                            vertical = 10.dp
                                        ), contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = level.name,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            CareerPilotCard {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Visibility
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            stringResource(R.string.create_challenge_visibility),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        ModernToggleRow(
                            options = ChallengeVisibility.entries.toList(),
                            selected = state.visibility,
                            onSelected = { onAction(CreateChallengeAction.OnVisibilityChanged(it)) },
                            labelProvider = {
                                if (it == ChallengeVisibility.PUBLIC) stringResource(R.string.create_challenge_public) else stringResource(
                                    R.string.create_challenge_private
                                )
                            }
                        )
                    }

                    // Challenge Type
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            stringResource(R.string.create_challenge_type),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        ModernToggleRow(
                            options = ChallengeType.entries.toList(),
                            selected = state.challengeType,
                            onSelected = { onAction(CreateChallengeAction.OnChallengeTypeChanged(it)) },
                            labelProvider = {
                                if (it == ChallengeType.AUDIO_ONLY) stringResource(R.string.create_challenge_audio_only) else stringResource(
                                    R.string.create_challenge_video_audio
                                )
                            }
                        )
                    }

                    if (state.challengeType == ChallengeType.VIDEO_AND_AUDIO) {
                        Column(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    MaterialTheme.shapes.medium
                                )
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = true,
                                    onCheckedChange = { },
                                    enabled = false
                                )
                                Text(
                                    stringResource(R.string.create_challenge_analyze_face),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Checkbox(
                                    checked = state.analyzePosture,
                                    onCheckedChange = {
                                        onAction(
                                            CreateChallengeAction.OnPostureToggle(
                                                it
                                            )
                                        )
                                    }
                                )
                                Text(
                                    stringResource(R.string.create_challenge_analyze_posture),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = state.analyzeHands,
                                    onCheckedChange = {
                                        onAction(
                                            CreateChallengeAction.OnHandsToggle(
                                                it
                                            )
                                        )
                                    }
                                )
                                Text(
                                    stringResource(R.string.create_challenge_analyze_hands),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(R.string.create_challenge_questions),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    FilledIconButton(
                        onClick = { onAction(CreateChallengeAction.OnAddQuestion) },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Question")
                    }
                }
                Text(
                    text = stringResource(
                        R.string.create_challenge_approx_duration,
                        state.questions.size * 2
                    ),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        itemsIndexed(state.questions) { index, question ->
            OutlinedTextField(
                value = question,
                onValueChange = { onAction(CreateChallengeAction.OnQuestionTextChange(index, it)) },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                label = { Text(stringResource(R.string.question, index + 1)) },
                placeholder = { Text(stringResource(R.string.enter_technical_question)) },
                trailingIcon = {
                    if (state.questions.size > 10) {
                        IconButton(onClick = { onAction(CreateChallengeAction.OnRemoveQuestion(index)) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(R.string.remove_question),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            CareerPilotButton(
                text = stringResource(R.string.create_challenge_submit),
                onClick = { onAction(CreateChallengeAction.OnSubmit) },
                enabled = !state.isSubmitting
            )
        }
    }
}

@Composable
fun <T> ModernToggleRow(
    options: List<T>,
    selected: T,
    onSelected: (T) -> Unit,
    labelProvider: @Composable (T) -> String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                MaterialTheme.shapes.large
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            val bgColor by animateColorAsState(
                if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                label = "toggleBg"
            )
            val contentColor by animateColorAsState(
                if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "toggleText"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(MaterialTheme.shapes.large)
                    .fillMaxHeight()
                    .background(bgColor, MaterialTheme.shapes.large)
                    .clickable(
                        onClick = { onSelected(option) },
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = labelProvider(option),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = contentColor
                )
            }
        }
    }
}
