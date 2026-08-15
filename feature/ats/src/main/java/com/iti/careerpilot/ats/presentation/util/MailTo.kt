package com.iti.careerpilot.ats.presentation.util

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri


fun composeEmail(
    context: Context,
    draft: EmailDraft,
): Boolean {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = "mailto:".toUri()
        putExtra(Intent.EXTRA_SUBJECT, draft.subject.asString(context))
        putExtra(Intent.EXTRA_TEXT, draft.body)
    }
    return if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
        true
    } else {
        false
    }
}
