package com.iti.onboarding.util
import android.content.Context
import android.text.format.Formatter


fun Long.toLocalizedFileSize(context: Context): String {
    return Formatter.formatShortFileSize(
        context,
        coerceAtLeast(0L),
    )
}