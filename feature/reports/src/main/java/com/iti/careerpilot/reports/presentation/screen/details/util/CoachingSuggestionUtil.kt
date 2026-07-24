package com.iti.careerpilot.reports.presentation.screen.details.util

import androidx.annotation.DrawableRes
import com.iti.careerpilot.reports.R
import com.iti.careerpilot.reports.domain.model.CoachingImpact

@DrawableRes
fun suggestionIcon(impact: CoachingImpact): Int = when (impact) {
    CoachingImpact.HIGH -> R.drawable.ic_coaching_target
    CoachingImpact.MEDIUM -> R.drawable.ic_coaching_trend
    CoachingImpact.LOW -> R.drawable.ic_coaching_story
}