package com.iti.careerpilot.quiz.presentation.screen.components

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.components.ButtonVariant
import com.iti.careerpilot.core.designsystem.components.CareerPilotButton
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
import com.iti.careerpilot.quiz.R

enum class SeniorityLevel(val apiKey: String, @StringRes val labelRes: Int) {
    ENTRY_LEVEL("Entry-level", R.string.seniority_entry_level),
    JUNIOR("Junior", R.string.seniority_junior),
    MID_LEVEL("Mid-level", R.string.seniority_mid_level),
    SENIOR("Senior", R.string.seniority_senior),
    LEAD("Lead", R.string.seniority_lead),
    EXECUTIVE("Executive", R.string.seniority_executive)
}

@Composable
fun SelectSeniorityContent(
    onSenioritySelected: (SeniorityLevel) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(
                modifier = Modifier.padding(bottom = Dimens.SpaceS),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.quiz_select_seniority),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.quiz_select_seniority_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(
            items = SeniorityLevel.entries,
            key = { it.apiKey }
        ) { level ->
            SeniorityItem(
                label = stringResource(level.labelRes),
                onClick = { onSenioritySelected(level) }
            )
        }
    }
}

@Composable
private fun SeniorityItem(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CareerPilotButton(
        text = label,
        onClick = onClick,
        modifier = modifier,
        variant = ButtonVariant.OUTLINE,
    )
}