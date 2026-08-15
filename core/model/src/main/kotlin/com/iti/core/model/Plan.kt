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
}
