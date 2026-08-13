package com.iti.careerpilot.ats.presentation.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

fun copyText(
    context: Context,
    label: String,
    value: String,
) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
}
