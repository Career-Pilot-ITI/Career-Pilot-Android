package com.iti.careerpilot.ats.domain.util

import java.net.IDN
import java.net.URI

object JobUrlParser {
    const val MAX_RAW_INPUT_LENGTH = 32_768
    const val MAX_URL_LENGTH = 2_048

    fun firstValidHttpsUrl(rawInput: String?): String? {
        val raw = rawInput?.takeIf { it.length <= MAX_RAW_INPUT_LENGTH } ?: return null
        return HTTPS_CANDIDATE.findAll(raw)
            .map { it.value.trimTrailingPunctuation() }
            .firstOrNull(::isValidHttpsUrl)
    }

    fun isValidHttpsUrl(value: String): Boolean {
        val candidate = value.trim()
        if (candidate.isEmpty() || candidate.length > MAX_URL_LENGTH) return false
        return try {
            val uri = URI(candidate)
            val host = uri.host?.let(IDN::toASCII)?.lowercase() ?: return false
            uri.scheme.equals(HTTPS_SCHEME, ignoreCase = true) &&
                uri.rawUserInfo == null &&
                uri.port in listOf(NO_PORT, HTTPS_PORT) &&
                host.contains('.') &&
                host !in BLOCKED_HOSTS &&
                !host.endsWith(LOCAL_SUFFIX) &&
                !host.isLoopbackAddress()
        } catch (_: Exception) {
            false
        }
    }

    private fun String.trimTrailingPunctuation(): String = trimEnd { char ->
        char in TRAILING_PUNCTUATION
    }

    private fun String.isLoopbackAddress(): Boolean =
        this == IPV6_LOOPBACK || startsWith(IPV4_LOOPBACK_PREFIX)

    private const val HTTPS_SCHEME = "https"
    private const val HTTPS_PORT = 443
    private const val NO_PORT = -1
    private const val LOCAL_SUFFIX = ".localhost"
    private const val IPV6_LOOPBACK = "::1"
    private const val IPV4_LOOPBACK_PREFIX = "127."
    private val HTTPS_CANDIDATE = Regex("https://[^\\s<>\\\"]+", RegexOption.IGNORE_CASE)
    private val TRAILING_PUNCTUATION = setOf('.', ',', ';', ':', '!', '?', ')', ']', '}')
    private val BLOCKED_HOSTS = setOf("localhost", "0.0.0.0")
}
