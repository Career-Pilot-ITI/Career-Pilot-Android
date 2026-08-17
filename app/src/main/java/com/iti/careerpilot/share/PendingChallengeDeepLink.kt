package com.iti.careerpilot.share

import android.content.Intent
import java.net.URI

data class PendingChallengeDeepLink(
    val challengeId: String,
    val invitationCode: String? = null
) {
    companion object {
        private const val WEB_HOST = "career-pilot-indol.vercel.app"
        private const val CUSTOM_SCHEME = "careerpilot"
        private const val CHALLENGE_HOST = "challenge"
        private const val CHALLENGE_PATH_SEGMENT = "challenge"

        fun from(rawUri: String?): PendingChallengeDeepLink? {
            if (rawUri.isNullOrBlank()) return null
            val uri = runCatching { URI(rawUri.trim()) }.getOrNull() ?: return null

            val scheme = uri.scheme?.lowercase() ?: return null
            val host = uri.host?.lowercase() ?: ""
            val path = uri.path.orEmpty()
            val segments = path.split('/').filter { it.isNotBlank() }

            val isUniversalLink = (scheme == "https" || scheme == "http") &&
                host == WEB_HOST &&
                segments.contains(CHALLENGE_PATH_SEGMENT)

            val isCustomSchemeLink = scheme == CUSTOM_SCHEME &&
                (host == CHALLENGE_HOST || segments.contains(CHALLENGE_PATH_SEGMENT))

            if (!isUniversalLink && !isCustomSchemeLink) return null

            val queryParams = uri.rawQuery
                ?.split('&')
                ?.mapNotNull { param ->
                    val parts = param.split('=', limit = 2)
                    if (parts.isNotEmpty() && parts[0].isNotBlank()) {
                        parts[0].trim() to parts.getOrNull(1)?.trim()
                    } else null
                }
                ?.toMap()
                .orEmpty()

            // 1. Check query parameter `id`
            var challengeId = queryParams["id"]?.takeIf { it.isNotBlank() }

            // 2. Fallback to path segment: /challenge/{id} or custom scheme /chl_abc
            if (challengeId.isNullOrBlank()) {
                val index = segments.indexOf(CHALLENGE_PATH_SEGMENT)
                if (index != -1 && index + 1 < segments.size) {
                    challengeId = segments[index + 1].trim().takeIf { it.isNotBlank() }
                } else if (isCustomSchemeLink && segments.isNotEmpty() && host == CHALLENGE_HOST) {
                    challengeId = segments.first().trim().takeIf { it.isNotBlank() }
                }
            }

            if (challengeId.isNullOrBlank()) return null
            val code = queryParams["code"]?.takeIf { it.isNotBlank() }

            return PendingChallengeDeepLink(
                challengeId = challengeId,
                invitationCode = code
            )
        }
    }
}

fun Intent.toPendingChallengeDeepLink(): PendingChallengeDeepLink? {
    if (action != Intent.ACTION_VIEW) return null
    return PendingChallengeDeepLink.from(dataString)
}
