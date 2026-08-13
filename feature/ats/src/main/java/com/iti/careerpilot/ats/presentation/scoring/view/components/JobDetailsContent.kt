package com.iti.careerpilot.ats.presentation.scoring.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.ats.R
import com.iti.careerpilot.ats.domain.model.JobListing
import com.iti.careerpilot.ats.domain.model.JobWorkspace
import com.iti.careerpilot.ats.presentation.scoring.uimodel.OverviewValue
import com.iti.careerpilot.core.designsystem.CareerPilotTheme
import com.iti.careerpilot.core.designsystem.components.ButtonVariant
import com.iti.careerpilot.core.designsystem.components.CareerPilotButton
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard

@Composable
internal fun JobDetailsContent(
    workspace: JobWorkspace,
    wasInterrupted: Boolean,
    hasInsufficientCoins: Boolean,
    errorMessage: String?,
    onStartScoring: () -> Unit,
    onOpenCoins: () -> Unit,
    onOpenJob: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { Spacer(Modifier.height(2.dp)) }

            item { JobHeaderCard(job = workspace.job, onOpenJob = onOpenJob) }
            item { JobOverviewCard(job = workspace.job) }

            if (workspace.job.description.isNotBlank()) {
                item { DescriptionCard(workspace.job.description) }
            }
            if (
                workspace.job.requiredSkills.isNotEmpty() ||
                workspace.job.preferredSkills.isNotEmpty() ||
                workspace.job.technologies.isNotEmpty()
            ) {
                item { RequirementsCard(workspace.job) }
            }

            item { Spacer(Modifier.height(8.dp)) }

            if (wasInterrupted) {
                item {
                    Text(
                        text = stringResource(R.string.ats_scoring_interrupted),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
            errorMessage?.let { message ->
                item { Text(text = message, color = MaterialTheme.colorScheme.error) }
            }
        }

        if (hasInsufficientCoins) {
            CareerPilotButton(
                text = stringResource(R.string.ats_get_coins),
                onClick = onOpenCoins,
                variant = ButtonVariant.OUTLINE,
            )
        } else {
            CareerPilotButton(
                text = stringResource(R.string.ats_start_scoring),
                onClick = onStartScoring,
            )
        }
    }
}

@Composable
private fun JobOverviewCard(job: JobListing) {
    val items = buildList {
        job.employmentType?.let {
            add(OverviewValue(R.drawable.ic_bag, R.string.ats_employment_type, it))
        }
        listOfNotNull(
            job.seniorityLevel,
            job.experienceYears?.let { stringResource(R.string.ats_experience_years, it) },
        ).joinToString(" · ").takeIf(String::isNotBlank)?.let {
            add(OverviewValue(R.drawable.ic_graduation, R.string.ats_experience_level, it))
        }
        job.postedLabel?.let {
            add(OverviewValue(R.drawable.ic_clock, R.string.ats_posted, it))
        }
        job.applicantsLabel?.let {
            add(OverviewValue(R.drawable.ic_person, R.string.ats_applied, it))
        }
    }

    CareerPilotCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.ats_job_overview),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            items.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    rowItems.forEach { item -> OverviewItem(item, Modifier.weight(1f)) }
                    if (rowItems.size == 1) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun OverviewItem(value: OverviewValue, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .3f),
        ) {
            Icon(
                painter = painterResource(value.resourceId),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(10.dp),
            )
        }
        Column {
            Text(
                text = stringResource(value.label).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value.value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun DescriptionCard(description: String) {
    CareerPilotCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.ats_description),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RequirementsCard(job: JobListing) {
    val colors = CareerPilotTheme.extendedColors

    CareerPilotCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = stringResource(R.string.ats_requirements),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            SkillGroup(
                title = stringResource(R.string.ats_skills_count, job.requiredSkills.size),
                values = job.requiredSkills,
                containerColor = colors.successContainer,
                contentColor = colors.onSuccessContainer,
            )
            SkillGroup(
                title = stringResource(
                    R.string.ats_preferred_skills_count,
                    job.preferredSkills.size
                ),
                values = job.preferredSkills,
                containerColor = colors.warningContainer,
                contentColor = colors.onWarningContainer,
            )
            SkillGroup(
                title = stringResource(R.string.ats_technologies_count, job.technologies.size),
                values = job.technologies,
                containerColor = colors.infoContainer,
                contentColor = colors.onInfoContainer,
            )
        }
    }
}

@Composable
private fun SkillGroup(
    title: String,
    values: List<String>,
    containerColor: Color,
    contentColor: Color,
) {
    if (values.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            values.forEach { value ->
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = containerColor,
                    contentColor = contentColor,
                ) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    )
                }
            }
        }
    }
}

