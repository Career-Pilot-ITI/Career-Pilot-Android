package com.iti.careerpilot.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project

class FeatureDomainConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("careerpilot.kotlin.jvm")
        addLibrary("implementation", "kotlinx-coroutines-core")
        addLibrary("testImplementation", "kotlinx-coroutines-test")
    }
}
