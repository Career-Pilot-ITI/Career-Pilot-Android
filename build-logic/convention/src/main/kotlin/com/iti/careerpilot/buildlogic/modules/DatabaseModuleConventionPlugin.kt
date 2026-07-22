package com.iti.careerpilot.buildlogic.modules

import com.android.build.api.dsl.LibraryExtension
import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

class DatabaseModuleConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")
        pluginManager.apply("com.google.devtools.ksp")
        pluginManager.apply("com.google.dagger.hilt.android")

        extensions.configure<LibraryExtension> {
            namespace = "com.iti.core.database"
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

        extensions.configure<KspExtension> {
            arg(
                "room.schemaLocation",
                layout.projectDirectory.dir("schemas").asFile.absolutePath,
            )
            arg("room.generateKotlin", "true")
        }

        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
        fun add(configuration: String, alias: String) {
            dependencies.add(configuration, libs.findLibrary(alias).get())
        }

        add("implementation", "androidx-core-ktx")
        add("implementation", "immutable-collections")
        add("implementation", "hilt-android")
        add("ksp", "hilt-compiler")
        add("implementation", "androidx-room-runtime")
        add("implementation", "androidx-room-ktx")
        add("ksp", "androidx-room-compiler")
    }
}
