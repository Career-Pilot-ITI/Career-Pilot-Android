package com.iti.careerpilot.buildlogic

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class BodyLanguageModuleConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("careerpilot.android.library")
        pluginManager.apply("careerpilot.android.hilt")
        pluginManager.apply("careerpilot.testing")
        pluginManager.apply("careerpilot.kotlin.serialization")

        extensions.configure<LibraryExtension> {
            namespace = "com.iti.careerpilot.bodylanguage"
        }

        addLibrary("implementation", "mediapipe-tasks-vision")
        addBundle("implementation", "camerax")
    }
}
