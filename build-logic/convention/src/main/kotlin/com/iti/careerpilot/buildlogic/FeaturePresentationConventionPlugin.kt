package com.iti.careerpilot.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project

class FeaturePresentationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("careerpilot.android.library")
        pluginManager.apply("careerpilot.android.compose")
        pluginManager.apply("careerpilot.android.hilt")

        addLibrary("implementation", "androidx-lifecycle-runtime-compose")
        addLibrary("implementation", "androidx-lifecycle-viewmodel-compose")
        addLibrary("implementation", "androidx-hilt-navigation-compose")
        addLibrary("implementation", "androidx-navigation3")
        addLibrary("implementation", "androidx-navigation3-ui")
        addLibrary("implementation", "androidx-lifecycle-viewmodel-navigation3")
        addLibrary("implementation", "kotlinx-coroutines-android")

        addLibrary("implementation", "androidx-material3-android")
        addLibrary("implementation", "coil-compose")
        addLibrary("implementation", "coil-network-ktor3")
        addLibrary("implementation", "lottie-compose")
        addLibrary("implementation", "kotlinx-datetime")
    }
}
