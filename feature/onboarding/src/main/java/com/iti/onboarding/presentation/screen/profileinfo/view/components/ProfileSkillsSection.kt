package com.iti.onboarding.presentation.screen.profileinfo.view.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iti.onboarding.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileSkillsSection(
    skills: ImmutableList<String>,
    allSkills: ImmutableList<String>,
    onSkillsChanged: (ImmutableList<String>) -> Unit,
    onAddSkillClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.profile_info_skills_detected_label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            allSkills.forEach { skill ->
                val isSelected = skills.contains(skill)
                SuggestionChip(
                    onClick = {
                        val newSkills = if (isSelected) {
                            (skills - skill).toImmutableList()
                        } else {
                            (skills + skill).toImmutableList()
                        }
                        onSkillsChanged(newSkills)
                    },
                    label = { 
                        Text(
                            skill, 
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary 
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                        ) 
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary 
                                         else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    border = null,
                    shape = CircleShape
                )
            }
            
            SuggestionChip(
                onClick = onAddSkillClicked,
                label = { 
                    Text(
                        stringResource(R.string.profile_info_other_skill), 
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ) 
                },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = null,
                shape = CircleShape
            )
        }
    }
}
