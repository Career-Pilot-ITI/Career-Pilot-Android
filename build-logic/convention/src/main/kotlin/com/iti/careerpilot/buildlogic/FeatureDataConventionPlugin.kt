package com.iti.careerpilot.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project

class FeatureDataConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("careerpilot.android.library")
        pluginManager.apply("careerpilot.android.hilt")
        pluginManager.apply("careerpilot.kotlin.serialization")

        addLibrary("implementation", "kotlinx-coroutines-core")
        addLibrary("implementation", "kotlinx-coroutines-android")
        addLibrary("testImplementation", "kotlinx-coroutines-test")
    }
}
