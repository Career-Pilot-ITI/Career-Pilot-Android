package com.iti.careerpilot.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidWorkManagerConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("careerpilot.android.hilt")

        addLibrary("implementation", "androidx-work-runtime-ktx")
        addLibrary("implementation", "androidx-hilt-work")
        addLibrary("ksp", "androidx-hilt-compiler")
    }
}
