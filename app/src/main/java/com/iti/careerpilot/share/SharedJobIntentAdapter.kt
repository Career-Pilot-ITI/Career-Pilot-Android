package com.iti.careerpilot.share

import android.content.Intent

fun Intent.toPendingSharedText(): PendingSharedText? {
    if (action != Intent.ACTION_SEND || type != TEXT_PLAIN_MIME_TYPE) return null
    return PendingSharedText.from(getCharSequenceExtra(Intent.EXTRA_TEXT))
}

private const val TEXT_PLAIN_MIME_TYPE = "text/plain"
