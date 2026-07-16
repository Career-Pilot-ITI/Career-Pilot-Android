package com.iti.careerpilot.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidHiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.google.devtools.ksp")
        pluginManager.apply("com.google.dagger.hilt.android")

        addLibrary("implementation", "hilt-android")
        addLibrary("ksp", "hilt-compiler")

        addLibrary("testImplementation", "hilt-android-testing")
        addLibrary("androidTestImplementation", "hilt-android-testing")
        addLibrary("kspTest", "hilt-compiler")
        addLibrary("kspAndroidTest", "hilt-compiler")
    }
}
