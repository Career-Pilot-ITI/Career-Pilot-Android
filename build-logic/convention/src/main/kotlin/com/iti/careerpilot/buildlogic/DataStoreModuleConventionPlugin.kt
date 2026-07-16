package com.iti.careerpilot.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project

class DataStoreModuleConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("careerpilot.android.library")
        pluginManager.apply("careerpilot.android.hilt")
        pluginManager.apply("careerpilot.android.datastore")
    }
}
