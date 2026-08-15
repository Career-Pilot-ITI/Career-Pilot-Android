package com.iti.careerpilot.share

@JvmInline
value class PendingSharedText private constructor(val value: String) {
    companion object {
        const val MAX_CAPTURED_LENGTH = 32_769

        fun from(rawText: CharSequence?): PendingSharedText? {
            val value = rawText?.toString()?.take(MAX_CAPTURED_LENGTH)?.takeIf(String::isNotBlank)
            return value?.let(::PendingSharedText)
        }
    }
}
