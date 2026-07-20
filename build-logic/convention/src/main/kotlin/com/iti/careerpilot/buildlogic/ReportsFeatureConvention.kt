package com.iti.careerpilot.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project

class ReportsFeatureConvention : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("careerpilot.feature.presentation")
        pluginManager.apply("careerpilot.android.ktor")
        pluginManager.apply("careerpilot.testing")

        addLibrary("implementation", "kotlinx-collections-immutable")
    }
}
