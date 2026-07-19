package com.iti.onboarding.util

fun Long.toMegabytes(): Float {
    return this / (1024f * 1024f)
}