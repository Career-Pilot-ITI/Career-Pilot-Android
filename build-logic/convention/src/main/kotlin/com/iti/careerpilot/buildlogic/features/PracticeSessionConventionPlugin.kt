package com.iti.careerpilot.buildlogic.features

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class PracticeSessionConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("careerpilot.feature.presentation")
        pluginManager.apply("careerpilot.testing")

        extensions.configure<LibraryExtension> {
            namespace = "com.iti.careerpilot.practicesession"
        }
    }
}
