package com.iti.careerpilot.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

class KotlinJvmConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.jvm")

        extensions.configure<KotlinJvmProjectExtension> {
            /*
             * Compile with the JVM already running Gradle instead of requesting
             * a separately installed JDK 17 toolchain.
             */
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_11)
            }
        }

        extensions.configure<JavaPluginExtension> {
            sourceCompatibility = CareerPilotBuildConfig.ANDROID_JAVA_VERSION
            targetCompatibility = CareerPilotBuildConfig.ANDROID_JAVA_VERSION
        }
    }
}
