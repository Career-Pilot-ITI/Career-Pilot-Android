package com.iti.careerpilot.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project

class SettingsConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("careerpilot.feature.presentation")
        pluginManager.apply("careerpilot.kotlin.serialization")
        pluginManager.apply("careerpilot.testing")
    }
}
