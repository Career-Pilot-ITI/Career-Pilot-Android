package com.iti.careerpilot.ats.presentation.util

import com.iti.careerpilot.ats.domain.model.JobWorkspace
import com.iti.common.util.UIText

data class EmailDraft(
    val subject: UIText,
    val body: String,
)

fun coverLetterEmailDraft(
    workspace: JobWorkspace?,
    body: String,
    genericSubject: UIText,
): EmailDraft {
    val title = workspace?.job?.title.orEmpty().trim()
    val company = workspace?.job?.companyName.orEmpty().trim()
    val subject = when {
        title.isNotEmpty() && company.isNotEmpty() -> UIText.DynamicString("$title — $company")
        title.isNotEmpty() -> UIText.DynamicString(title)
        else -> genericSubject
    }
    return EmailDraft(subject = subject, body = body)
}
