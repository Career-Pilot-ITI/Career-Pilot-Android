package com.iti.careerpilot.ats.presentation.scoring.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import com.iti.careerpilot.ats.presentation.scoring.uimodel.OverviewValue
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard

@Composable
fun JobOverviewCard(job: JobListing) {
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