package com.iti.careerpilot.ats.presentation.jobdetails.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.ats.R
import com.iti.careerpilot.ats.domain.model.JobListing
import com.iti.careerpilot.core.designsystem.CareerPilotTheme
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard


@Composable
fun RequirementsCard(job: JobListing) {
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
