package com.iti.careerpilot.buildlogic.modules

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

class DesignSystemModuleConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        extensions.configure<LibraryExtension> {
            namespace = "com.iti.careerpilot.core.designsystem"
            compileSdk {
                version = release(37)
            }
            defaultConfig {
                minSdk = 26
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_11
                targetCompatibility = JavaVersion.VERSION_11
            }
            buildFeatures {
                buildConfig = false
                compose = true
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

        dependencies.add(
            "implementation",
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

        add("api", "androidx-material3-android")
        add("api", "coil-compose")
        add("api", "extended-icons")
        add("implementation", "androidx-compose-googlefonts")
        add("implementation", "androidx-compose-material-icons-core")
        add("implementation", "compose-material3")
        add("implementation", "lottie-compose")
        add("implementation", "androidx-lifecycle-runtime-compose")
        add("implementation", "kotlinx-coroutines-android")
    }
}
