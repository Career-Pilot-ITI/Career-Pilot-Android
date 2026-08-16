package com.iti.core.model

enum class Plan {
    FREE,
    PLUS,
    MAX;

    fun displayName(): String = when (this) {
        FREE -> "Free"
        PLUS -> "Plus"
        MAX  -> "Max"
    }

    companion object {
        fun fromString(raw: String): Plan = when (raw.uppercase().trim()) {
            "PLUS"       -> PLUS
            "PRO", "MAX" -> MAX
            else         -> FREE
        }
    }
}
