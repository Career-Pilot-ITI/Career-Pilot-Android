package com.iti.careerpilot.ats.presentation.jobdetails.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.ats.R
import com.iti.careerpilot.ats.presentation.components.AtsCenteredTopBar
import com.iti.careerpilot.ats.presentation.components.JobHeaderCard
import com.iti.careerpilot.ats.presentation.jobdetails.view.components.DescriptionCard
import com.iti.careerpilot.ats.presentation.jobdetails.view.components.JobOverviewCard
import com.iti.careerpilot.ats.presentation.jobdetails.view.components.RequirementsCard
import com.iti.careerpilot.ats.presentation.scoring.state.ScoringAction
import com.iti.careerpilot.ats.presentation.scoring.state.ScoringUiState
import com.iti.careerpilot.core.designsystem.components.ButtonVariant
import com.iti.careerpilot.core.designsystem.components.CareerPilotButton

@Composable
fun JobDetailsScreen(
    state: ScoringUiState,
    onAction: (ScoringAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenJob: (String) -> Unit = {},
) {
    val workspace = requireNotNull(state.workspace)
    val errorMessage = state.error?.asString()

    Column(modifier = modifier.fillMaxSize()) {
        AtsCenteredTopBar(
            title = stringResource(R.string.ats_job_description_title),
            onBack = onBack,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
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

                if (state.wasInterrupted) {
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

            if (state.hasInsufficientCoins) {
                CareerPilotButton(
                    text = stringResource(R.string.ats_get_coins),
                    onClick = { onAction(ScoringAction.OpenCoins) },
                    variant = ButtonVariant.OUTLINE,
                )
            } else {
                CareerPilotButton(
                    text = stringResource(R.string.ats_start_scoring),
                    onClick = { onAction(ScoringAction.StartScore) },
                )
            }
        }
    }
}
