package com.iti.careerpilot.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project


class OnboardingConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply("careerpilot.android.library")
        pluginManager.apply("careerpilot.android.compose")
        pluginManager.apply("careerpilot.android.hilt")
        pluginManager.apply("careerpilot.kotlin.serialization")

        addLibrary(
            configuration = "implementation",
            alias = "androidx-activity-compose",
        )

        addLibrary(
            configuration = "implementation",
            alias = "androidx-lifecycle-runtime-compose",
        )

        addLibrary(
            configuration = "implementation",
            alias = "androidx-lifecycle-viewmodel-compose",
        )

        addLibrary(
            configuration = "implementation",
            alias = "androidx-material3-android",
        )

        addLibrary(
            configuration = "implementation",
            alias = "androidx-hilt-navigation-compose",
        )

        addLibrary(
            configuration = "implementation",
            alias = "androidx-navigation3",
        )

        addLibrary(
            configuration = "implementation",
            alias = "androidx-navigation3-ui",
        )

        addLibrary(
            configuration = "implementation",
            alias = "androidx-lifecycle-viewmodel-navigation3",
        )

        addLibrary(
            configuration = "implementation",
            alias = "kotlinx-coroutines-android",
        )

        addLibrary(
            configuration = "implementation",
            alias = "extended-icons",
        )
    }
}