package com.iti.careerpilot.buildlogic

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")

        extensions.configure<LibraryExtension> {
            configureCareerPilotLibrary()
        }
        configureCareerPilotKotlinAndroid()
        configureFlavors()

        addLibrary("implementation", "androidx-core-ktx")
        addLibrary("implementation", "immutable-collections")
    }
}
