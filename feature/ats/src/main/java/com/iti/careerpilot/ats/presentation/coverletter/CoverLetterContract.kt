package com.iti.careerpilot.ats.presentation.coverletter

import com.iti.careerpilot.ats.domain.model.JobWorkspace
import com.iti.careerpilot.ats.presentation.util.EmailDraft
import com.iti.common.util.UIText

data class CoverLetterUiState(
    val workspace: JobWorkspace? = null,
    val generatedValue: String = "",
    val editedValue: String = "",
    val approachTips: String? = null,
    val contactName: String = "",
    val contactEmail: String = "",
    val contactPhone: String = "",
    val coinCost: Int? = null,
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val isConfirmationVisible: Boolean = false,
    val wasInterrupted: Boolean = false,
    val hasInsufficientCoins: Boolean = false,
    val error: UIText? = null,
)

sealed interface CoverLetterAction {
    data object RequestGeneration : CoverLetterAction
    data object ConfirmGeneration : CoverLetterAction
    data object DismissConfirmation : CoverLetterAction
    data object ToggleEditing : CoverLetterAction
    data class EditedValueChanged(val value: String) : CoverLetterAction
    data object Copy : CoverLetterAction
    data object Email : CoverLetterAction
    data object OpenCoins : CoverLetterAction
}

sealed interface CoverLetterEffect {
    data class CopyText(val value: String) : CoverLetterEffect
    data class ComposeEmail(val draft: EmailDraft) : CoverLetterEffect
    data object OpenCoinsPaywall : CoverLetterEffect
}
