package com.iti.careerpilot.ats.presentation.jobdetails

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.ats.R
import com.iti.careerpilot.ats.domain.model.JobListing
import com.iti.careerpilot.ats.domain.util.JobDisplayMetadataResolver
import com.iti.careerpilot.ats.domain.util.WorkArrangement
import com.iti.careerpilot.core.designsystem.components.CareerPilotButton
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
import com.iti.careerpilot.core.designsystem.common.ObserveEvent

@Composable
fun JobDetailsRoot(
    workspaceId: Long,
    onBack: () -> Unit,
    onStartScoring: (Long) -> Unit,
    viewModel: JobDetailsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(workspaceId) { viewModel.loadWorkspace(workspaceId) }
    ObserveEvent(viewModel.effects) { effect ->
        when (effect) {
            is JobDetailsEffect.NavigateToScore -> onStartScoring(effect.workspaceId)
            is JobDetailsEffect.OpenExternalUrl -> {
                val intent = Intent(Intent.ACTION_VIEW, effect.url.toUri())
                if (intent.resolveActivity(context.packageManager) != null) context.startActivity(intent)
            }
        }
    }
    JobDetailsScreen(state, viewModel::onAction, onBack)
}

@Composable
fun JobDetailsScreen(
    state: JobDetailsUiState,
    onAction: (JobDetailsAction) -> Unit,
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
            TextButton(onClick = onBack) { Text(stringResource(R.string.ats_back)) }
            Text(
                text = stringResource(R.string.job_details),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
        }
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.error != null -> Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(state.error.asString())
                CareerPilotButton(
                    text = stringResource(R.string.ats_retry),
                    onClick = { onAction(JobDetailsAction.RetryClicked) },
                    modifier = Modifier.padding(20.dp),
                )
            }
            state.workspace != null -> JobDetailsContent(
                job = state.workspace.job,
                onAction = onAction,
            )
        }
    }
}

@Composable
private fun JobDetailsContent(
    job: JobListing,
    onAction: (JobDetailsAction) -> Unit,
) {
    val metadata = JobDisplayMetadataResolver.resolve(job)
    val externalUrlAvailable = listOfNotNull(job.sourceUrl, job.applicationUrl)
        .any(com.iti.careerpilot.ats.domain.util.JobUrlParser::isValidHttpsUrl)
    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                CareerPilotCard(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            Modifier
                                .size(48.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                job.companyName.firstOrNull()?.uppercase() ?: "?",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Column(Modifier.weight(1f)) {
                            Text(job.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            val companyLine = listOf(job.companyName, job.location)
                                .filter(String::isNotBlank).joinToString(" · ")
                            if (companyLine.isNotBlank()) Text(
                                companyLine,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        if (externalUrlAvailable) TextButton(
                            onClick = { onAction(JobDetailsAction.ExternalLinkClicked) },
                        ) { Text(stringResource(R.string.ats_open_job)) }
                    }
                }
            }
            item {
                DetailsSection(stringResource(R.string.ats_job_overview)) {
                    OverviewValue(stringResource(R.string.ats_employment_type), job.employmentType)
                    OverviewValue(stringResource(R.string.ats_seniority), job.seniorityLevel)
                    metadata.experienceYears?.let {
                        OverviewValue(
                            stringResource(R.string.ats_experience),
                            stringResource(R.string.ats_experience_years, it),
                        )
                    }
                    metadata.workArrangement?.let {
                        OverviewValue(
                            stringResource(R.string.ats_work_arrangement),
                            stringResource(
                                when (it) {
                                    WorkArrangement.REMOTE -> R.string.ats_remote
                                    WorkArrangement.HYBRID -> R.string.ats_hybrid
                                    WorkArrangement.ON_SITE -> R.string.ats_on_site
                                },
                            ),
                        )
                    }
                }
            }
            if (job.description.isNotBlank()) item {
                DetailsSection(stringResource(R.string.ats_description)) { Text(job.description) }
            }
            if (job.requiredSkills.isNotEmpty()) item {
                ListSection(stringResource(R.string.ats_required_skills), job.requiredSkills)
            }
            if (job.preferredSkills.isNotEmpty()) item {
                ListSection(stringResource(R.string.ats_preferred_skills), job.preferredSkills)
            }
            if (job.technologies.isNotEmpty()) item {
                ListSection(stringResource(R.string.ats_technologies), job.technologies)
            }
            if (job.responsibilities.isNotEmpty()) item {
                ListSection(stringResource(R.string.ats_responsibilities), job.responsibilities)
            }
            if (job.qualifications.isNotEmpty()) item {
                ListSection(stringResource(R.string.ats_qualifications), job.qualifications)
            }
        }
        CareerPilotButton(
            text = stringResource(R.string.ats_start_scoring),
            onClick = { onAction(JobDetailsAction.ScoreClicked) },
            modifier = Modifier.padding(20.dp),
        )
    }
}

@Composable
private fun DetailsSection(title: String, content: @Composable () -> Unit) {
    CareerPilotCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
private fun ListSection(title: String, values: List<String>) = DetailsSection(title) {
    values.forEach {
        Text(stringResource(R.string.ats_list_item, it), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun OverviewValue(label: String, value: String?) {
    if (!value.isNullOrBlank()) {
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}
