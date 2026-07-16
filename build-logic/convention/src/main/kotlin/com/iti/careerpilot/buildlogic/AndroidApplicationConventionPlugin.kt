package com.iti.careerpilot.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")

        extensions.configure<ApplicationExtension> {
            configureCareerPilotApplication()
        }
        configureCareerPilotKotlinAndroid()

        addLibrary("implementation", "androidx-core-ktx")
        addLibrary("implementation", "androidx-lifecycle-runtime-ktx")
    }
}
