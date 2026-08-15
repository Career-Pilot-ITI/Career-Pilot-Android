package com.iti.careerpilot.optimization

import android.content.Intent
import java.net.URI

data class PendingCvOptimization(
    val workspaceId: Long,
    val jobId: Long,
) {
    companion object {
        fun from(rawUri: String?): PendingCvOptimization? {
            val uri = rawUri?.let { value -> runCatching { URI(value) }.getOrNull() } ?: return null
            if (uri.scheme != SCHEME || uri.host != HOST) return null
            val segments = uri.path
                ?.split('/')
                .orEmpty()
                .filter(String::isNotBlank)
            if (segments.size != 3 || segments.first() != RESULT_PATH) return null
            val workspaceId = segments[1].toLongOrNull() ?: return null
            val jobId = segments[2].toLongOrNull() ?: return null
            if (workspaceId < 0L || jobId < 0L) return null
            return PendingCvOptimization(workspaceId = workspaceId, jobId = jobId)
        }
    }
}

fun Intent.toPendingCvOptimization(): PendingCvOptimization? {
    if (action != Intent.ACTION_VIEW) return null
    return PendingCvOptimization.from(dataString)
}

private const val SCHEME = "careerpilot"
private const val HOST = "ats"
private const val RESULT_PATH = "optimized-cv"
