package com.iti.careerpilot.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Opt-in testing setup for Kotlin/JVM and Android modules.
 *
 * Apply this plugin only to modules that contain tests. Dependencies are added
 * after the module's base plugin is available, so the plugin is order-safe in
 * the plugins block.
 */
class TestingConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
            configureUnitTestingDependencies()
        }

        pluginManager.withPlugin("com.android.application") {
            extensions.configure<ApplicationExtension> {
                defaultConfig {
                    testInstrumentationRunner = CareerPilotBuildConfig.TEST_RUNNER
                }
                testOptions {
                    unitTests.isIncludeAndroidResources = true
                }
            }
            configureUnitTestingDependencies()
            configureAndroidTestingDependencies()
        }

        pluginManager.withPlugin("com.android.library") {
            extensions.configure<LibraryExtension> {
                defaultConfig {
                    testInstrumentationRunner = CareerPilotBuildConfig.TEST_RUNNER
                }
                testOptions {
                    unitTests.isIncludeAndroidResources = true
                }
            }
            configureUnitTestingDependencies()
            configureAndroidTestingDependencies()
        }
    }
}

private fun Project.configureUnitTestingDependencies() {
    addLibrary("testImplementation", "junit")
    addLibrary("testImplementation", "kotlinx-coroutines-test")
}

private fun Project.configureAndroidTestingDependencies() {
    addLibrary("androidTestImplementation", "androidx-junit")
    addLibrary("androidTestImplementation", "androidx-espresso-core")

    pluginManager.withPlugin("org.jetbrains.kotlin.plugin.compose") {
        addPlatform("androidTestImplementation", "androidx-compose-bom")
        addLibrary("androidTestImplementation", "androidx-compose-ui-test-junit4")
        addLibrary("debugImplementation", "androidx-compose-ui-test-manifest")
    }

    pluginManager.withPlugin("com.google.dagger.hilt.android") {
        addLibrary("testImplementation", "hilt-android-testing")
        addLibrary("androidTestImplementation", "hilt-android-testing")
        addLibrary("kspTest", "hilt-compiler")
        addLibrary("kspAndroidTest", "hilt-compiler")
    }

    pluginManager.withPlugin("careerpilot.android.room") {
        addLibrary("testImplementation", "androidx-room-testing")
    }
}
