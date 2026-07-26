package com.iti.careerpilot.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidDataStoreConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        addLibrary("implementation", "androidx-datastore")
    }
}
