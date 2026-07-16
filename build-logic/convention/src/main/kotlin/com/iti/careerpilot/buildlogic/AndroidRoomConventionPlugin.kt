package com.iti.careerpilot.buildlogic

import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidRoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.google.devtools.ksp")

        addLibrary("implementation", "androidx-room-runtime")
        addLibrary("implementation", "androidx-room-ktx")
        addLibrary("ksp", "androidx-room-compiler")
        addLibrary("testImplementation", "androidx-room-testing")

        extensions.configure<KspExtension> {
            arg(
                "room.schemaLocation",
                layout.projectDirectory.dir("schemas").asFile.absolutePath,
            )
            arg("room.generateKotlin", "true")
        }
    }
}
