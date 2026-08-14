package com.iti.careerpilot.ats.presentation.coverletter.state

import androidx.compose.runtime.Immutable
import com.iti.careerpilot.ats.domain.model.JobWorkspace
import com.iti.common.util.UIText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class CoverLetterUiState(
    val workspace: JobWorkspace? = null,
    val generatedValue: String = "",
    val editedValue: String = "",
    val approachTips: ImmutableList<String> = persistentListOf(),
    val contactName: String = "",
    val contactEmail: String = "",
    val contactPhone: String = "",
    val coinCost: Int? = null,
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val wasInterrupted: Boolean = false,
    val hasInsufficientCoins: Boolean = false,
    val error: UIText? = null,
)
