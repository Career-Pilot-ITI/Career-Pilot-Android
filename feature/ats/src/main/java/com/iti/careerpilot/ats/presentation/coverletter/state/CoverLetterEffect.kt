package com.iti.careerpilot.ats.presentation.coverletter.state

import com.iti.careerpilot.ats.presentation.util.EmailDraft

sealed interface CoverLetterEffect {
    data class CopyText(val value: String) : CoverLetterEffect
    data class ComposeEmail(val draft: EmailDraft) : CoverLetterEffect
    data object OpenCoinsPaywall : CoverLetterEffect
}
