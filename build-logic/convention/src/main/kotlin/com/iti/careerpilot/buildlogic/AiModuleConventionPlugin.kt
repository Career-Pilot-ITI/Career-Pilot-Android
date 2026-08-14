package com.iti.careerpilot.buildlogic

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AiModuleConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("careerpilot.android.library")
        pluginManager.apply("careerpilot.android.hilt")
        pluginManager.apply("careerpilot.testing")
        pluginManager.apply("careerpilot.kotlin.serialization")

        extensions.configure<LibraryExtension> {
            namespace = "com.iti.careerpilot.ai"

            defaultConfig {
                consumerProguardFiles("consumer-rules.pro")
            }
        }

        dependencies {
            val bom = libs.findLibrary("firebase-bom").get()
            add("implementation", platform(bom))
            add("implementation", libs.findLibrary("firebase-ai").get())
            add("implementation", libs.findLibrary("firebase-appcheck-playintegrity").get())
            add("implementation", libs.findLibrary("firebase-appcheck-debug").get())
            add("implementation", project(":core:model"))
            add("implementation", project(":core:common"))
        }
    }
}
