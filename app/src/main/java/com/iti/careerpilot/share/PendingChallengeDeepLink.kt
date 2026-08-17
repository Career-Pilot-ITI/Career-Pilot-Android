package com.iti.careerpilot.share

import android.content.Intent
import android.net.Uri

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
            val uri = runCatching { Uri.parse(rawUri.trim()) }.getOrNull() ?: return null

            val scheme = uri.scheme?.lowercase() ?: return null
            val host = uri.host?.lowercase() ?: ""
            val segments = uri.pathSegments.orEmpty()

            val isUniversalLink = (scheme == "https" || scheme == "http") &&
                host == WEB_HOST &&
                segments.contains(CHALLENGE_PATH_SEGMENT)

            val isCustomSchemeLink = scheme == CUSTOM_SCHEME &&
                (host == CHALLENGE_HOST || segments.contains(CHALLENGE_PATH_SEGMENT))

            if (!isUniversalLink && !isCustomSchemeLink) return null

            // 1. Check query parameter `id`
            var challengeId = runCatching { uri.getQueryParameter("id") }.getOrNull()?.takeIf { it.isNotBlank() }

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
            val code = runCatching { uri.getQueryParameter("code") }.getOrNull()?.takeIf { it.isNotBlank() }

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

