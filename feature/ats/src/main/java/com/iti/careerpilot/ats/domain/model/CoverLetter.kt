package com.iti.careerpilot.ats.domain.model

import kotlinx.collections.immutable.ImmutableList

data class CoverLetter(
    val body: String,
    val approachTips: ImmutableList<String>,
    val coinCost: Int?,
)
