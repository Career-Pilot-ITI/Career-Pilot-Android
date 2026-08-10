package com.iti.careerpilot.buildlogic.features

import com.android.build.api.dsl.LibraryExtension
import com.iti.careerpilot.buildlogic.addLibrary
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class ReportsFeatureConvention : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("careerpilot.feature.presentation")
        pluginManager.apply("careerpilot.android.ktor")
        pluginManager.apply("careerpilot.testing")

        extensions.configure<LibraryExtension> {
            namespace = "com.iti.careerpilot.reports"
        }

        addLibrary("implementation", "extended-icons")
        addLibrary("implementation", "androidx-paging-runtime")
        addLibrary("implementation", "androidx-paging-compose")
    }
}
