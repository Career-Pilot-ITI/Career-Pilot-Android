package com.iti.careerpilot.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project

class HomeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("careerpilot.feature.data")
        pluginManager.apply("careerpilot.feature.presentation")
        pluginManager.apply("careerpilot.android.ktor")
        pluginManager.apply("careerpilot.testing")
    }
}
