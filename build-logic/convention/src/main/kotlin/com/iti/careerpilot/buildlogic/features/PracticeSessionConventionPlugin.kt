package com.iti.careerpilot.buildlogic.features

import com.android.build.api.dsl.LibraryExtension
import com.iti.careerpilot.buildlogic.configureCareerPilotFlavors
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import kotlin.collections.plusAssign

class PracticeSessionConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
        pluginManager.apply("com.google.devtools.ksp")
        pluginManager.apply("com.google.dagger.hilt.android")
        pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")

        extensions.configure<LibraryExtension> {
            namespace = "com.iti.careerpilot.practicesession"
            compileSdk {
                version = release(37)
            }
            defaultConfig {
                minSdk = 26
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_11
                targetCompatibility = JavaVersion.VERSION_11
            }
            buildFeatures {
                buildConfig = false
                compose = true
            }
            testOptions {
                unitTests.isIncludeAndroidResources = true
            }
            configureCareerPilotFlavors()
            packaging {
                resources.excludes += setOf(
                    "/META-INF/{AL2.0,LGPL2.1}",
                    "META-INF/LICENSE*",
                    "META-INF/NOTICE*",
                )
            }
        }

        extensions.configure<KotlinAndroidProjectExtension> {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_11)
            }
        }

        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
        fun add(configuration: String, alias: String) {
            dependencies.add(configuration, libs.findLibrary(alias).get())
        }

        dependencies.add(
            "implementation",
            dependencies.platform(libs.findLibrary("androidx-compose-bom").get()),
        )
        dependencies.add(
            "androidTestImplementation",
            dependencies.platform(libs.findLibrary("androidx-compose-bom").get()),
        )

        add("implementation", "androidx-core-ktx")
        add("implementation", "immutable-collections")
        add("implementation", "androidx-activity-compose")
        add("implementation", "androidx-compose-ui")
        add("implementation", "androidx-compose-ui-graphics")
        add("implementation", "androidx-compose-ui-tooling-preview")
        add("implementation", "androidx-compose-material3")
        add("debugImplementation", "androidx-compose-ui-tooling")
        add("implementation", "hilt-android")
        add("ksp", "hilt-compiler")
        add("implementation", "kotlinx-serialization-json")

        add("implementation", "androidx-lifecycle-runtime-compose")
        add("implementation", "androidx-lifecycle-viewmodel-compose")
        add("implementation", "androidx-hilt-navigation-compose")
        add("implementation", "androidx-material3-android")
        add("implementation", "kotlinx-coroutines-android")
        add("implementation", "coil-compose")
        add("implementation", "coil-network-ktor3")
        add("implementation", "lottie-compose")

        add("testImplementation", "junit")
        add("testImplementation", "kotlinx-coroutines-test")
        add("testImplementation", "hilt-android-testing")
        add("androidTestImplementation", "androidx-junit")
        add("androidTestImplementation", "androidx-espresso-core")
        add("androidTestImplementation", "androidx-compose-ui-test-junit4")
        add("androidTestImplementation", "hilt-android-testing")
        add("debugImplementation", "androidx-compose-ui-test-manifest")
        add("kspTest", "hilt-compiler")
        add("kspAndroidTest", "hilt-compiler")
    }
}