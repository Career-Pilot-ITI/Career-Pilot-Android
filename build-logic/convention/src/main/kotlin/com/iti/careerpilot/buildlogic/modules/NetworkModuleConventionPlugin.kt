package com.iti.careerpilot.buildlogic.modules

import com.android.build.api.dsl.LibraryExtension
import java.util.Properties
import org.gradle.api.GradleException
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

class NetworkModuleConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")
        pluginManager.apply("com.google.devtools.ksp")
        pluginManager.apply("com.google.dagger.hilt.android")
        pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")

        val baseUrl = resolveBaseUrl()

        extensions.configure<LibraryExtension> {
            namespace = "com.iti.careerpilot.core.network"
            compileSdk {
                version = release(37)
            }
            defaultConfig {
                minSdk = 26
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                buildConfigField("String", "BASE_URL", "\"$baseUrl\"")
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_11
                targetCompatibility = JavaVersion.VERSION_11
            }
            buildFeatures {
                buildConfig = true
            }
            testOptions {
                unitTests.isIncludeAndroidResources = true
            }
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

        add("implementation", "androidx-core-ktx")
        add("implementation", "immutable-collections")
        add("implementation", "hilt-android")
        add("ksp", "hilt-compiler")
        add("implementation", "kotlinx-serialization-json")
        libs.findBundle("ktor").get().get().forEach { dependency ->
            dependencies.add("implementation", dependency)
        }
        add("implementation", "kotlinx-coroutines-core")

        add("testImplementation", "junit")
        add("testImplementation", "kotlinx-coroutines-test")
        add("testImplementation", "hilt-android-testing")
        add("androidTestImplementation", "androidx-junit")
        add("androidTestImplementation", "androidx-espresso-core")
        add("androidTestImplementation", "hilt-android-testing")
        add("kspTest", "hilt-compiler")
        add("kspAndroidTest", "hilt-compiler")
    }

    private fun Project.resolveBaseUrl(): String {
        val fromLocalProperties = Properties().run {
            val file = rootProject.file("local.properties")
            if (file.exists()) {
                file.inputStream().use(::load)
            }
            getProperty("careerpilot.baseUrl")
        }

        return listOf(
            fromLocalProperties,
            providers.gradleProperty("careerpilot.baseUrl").orNull,
            providers.environmentVariable("CAREERPILOT_BASE_URL").orNull,
        ).firstOrNull { !it.isNullOrBlank() }
            ?: throw GradleException(
                """
                Missing backend base URL for :core:network.

                Local development - add this line to local.properties in the project root:

                    careerpilot.baseUrl=http://10.0.2.2:8080
                """.trimIndent(),
            )
    }
}
