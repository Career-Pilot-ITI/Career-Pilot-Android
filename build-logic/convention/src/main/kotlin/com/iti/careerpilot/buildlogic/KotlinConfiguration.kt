package com.iti.careerpilot.buildlogic

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

internal fun Project.configureCareerPilotKotlinAndroid() {
    extensions.configure<KotlinAndroidProjectExtension> {
        compilerOptions {
            // Keep Kotlin and Java Android compilation on the same JVM target.
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
}
