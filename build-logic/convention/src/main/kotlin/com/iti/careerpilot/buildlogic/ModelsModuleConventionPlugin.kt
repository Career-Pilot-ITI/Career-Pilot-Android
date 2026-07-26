package com.iti.careerpilot.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project

class ModelsModuleConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("careerpilot.kotlin.jvm")
    }
}
