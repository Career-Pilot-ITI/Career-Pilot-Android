package com.iti.careerpilot.buildlogic

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")

        extensions.configure<LibraryExtension> {
            namespace = "com.iti.careerpilot"
            compileSdk {
                version = release(37)
            }

            defaultConfig {
                minSdk = 26
            }

            compileOptions {
                sourceCompatibility = javaVersion11
                targetCompatibility = javaVersion11
            }

            buildFeatures {
                buildConfig = true
            }

            flavorDimensions += "contentType"
            productFlavors {
                create("prod") {
                    dimension = "contentType"
                    buildConfigField("String", "FLAVOR_NAME", "\"prod\"")
                }
                create("fake") {
                    dimension = "contentType"
                    buildConfigField("String", "FLAVOR_NAME", "\"fake\"")
                }
            }

            packaging {
                resources.excludes += setOf(
                    "/META-INF/{AL2.0,LGPL2.1}",
                    "META-INF/LICENSE*",
                    "META-INF/NOTICE*",
                )
            }
        }

        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
        fun addLibrary(configuration: String, alias: String) {
            dependencies.add(configuration, libs.findLibrary(alias).get())
        }

        addLibrary("implementation", "androidx-core-ktx")
        addLibrary("implementation", "immutable-collections")
    }
}

private val javaVersion11 = org.gradle.api.JavaVersion.VERSION_11
