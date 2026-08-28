package com.iti.careerpilot.share

import android.content.Intent
import android.net.Uri

data class PendingCompanyInterviewDeepLink(
    val token: String
) {
    companion object {
        private const val WEB_HOST = "career-pilot-indol.vercel.app"
        private const val CUSTOM_SCHEME = "careerpilot"
        private const val INTERVIEW_HOST = "company-interview"
        private const val INTERVIEW_PATH_SEGMENT = "interview"

        fun from(rawUri: String?): PendingCompanyInterviewDeepLink? {
            if (rawUri.isNullOrBlank()) return null
            val uri = runCatching { Uri.parse(rawUri.trim()) }.getOrNull() ?: return null

            val scheme = uri.scheme?.lowercase() ?: return null
            val host = uri.host?.lowercase() ?: ""
            val segments = uri.pathSegments.orEmpty()

            val isUniversalLink = (scheme == "https" || scheme == "http") &&
                host == WEB_HOST &&
                segments.contains(INTERVIEW_PATH_SEGMENT)

            val isCustomSchemeLink = scheme == CUSTOM_SCHEME &&
                (host == INTERVIEW_HOST || host == "interview" || segments.contains("company-interview") || segments.contains(INTERVIEW_PATH_SEGMENT))

            if (!isUniversalLink && !isCustomSchemeLink) return null

            // 1. Check query parameter `token`
            var token = runCatching { uri.getQueryParameter("token") }.getOrNull()?.takeIf { it.isNotBlank() }

            // 2. Fallback to path segment: /interview/{token}
            if (token.isNullOrBlank()) {
                val index = segments.indexOf(INTERVIEW_PATH_SEGMENT)
                if (index != -1 && index + 1 < segments.size) {
                    token = segments[index + 1].trim().takeIf { it.isNotBlank() }
                } else if (isCustomSchemeLink && segments.isNotEmpty()) {
                    token = segments.first().trim().takeIf { it.isNotBlank() }
                }
            }

            if (token.isNullOrBlank()) return null

            return PendingCompanyInterviewDeepLink(token = token)
        }
    }
}

fun Intent.toPendingCompanyInterviewDeepLink(): PendingCompanyInterviewDeepLink? {
    val rawUri = dataString ?: data?.toString() ?: return null
    return PendingCompanyInterviewDeepLink.from(rawUri)
}
