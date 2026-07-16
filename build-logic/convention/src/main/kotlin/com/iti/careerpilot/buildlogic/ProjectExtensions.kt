package com.iti.careerpilot.buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependencyBundle
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

internal val Project.libs: VersionCatalog
    get() = extensions
        .getByType<VersionCatalogsExtension>()
        .named("libs")

/**
 * Resolves a version-catalog library alias to its concrete dependency notation.
 *
 * The previous implementation returned Provider<out Any>. Passing that erased
 * provider to DependencyHandler.platform(...) made Gradle 9 interpret the
 * provider's internal value-source map as dependency notation.
 */
internal fun Project.library(alias: String): MinimalExternalModuleDependency =
    libs.findLibrary(alias)
        .orElseThrow {
            IllegalArgumentException(
                "Library alias '$alias' was not found in gradle/libs.versions.toml"
            )
        }
        .get()

internal fun Project.bundle(alias: String): ExternalModuleDependencyBundle =
    libs.findBundle(alias)
        .orElseThrow {
            IllegalArgumentException(
                "Bundle alias '$alias' was not found in gradle/libs.versions.toml"
            )
        }
        .get()

internal fun Project.addLibrary(
    configuration: String,
    alias: String,
) {
    dependencies.add(configuration, library(alias))
}

internal fun Project.addBundle(
    configuration: String,
    alias: String,
) {
    bundle(alias).forEach { dependency ->
        dependencies.add(configuration, dependency)
    }
}

internal fun Project.addPlatform(
    configuration: String,
    alias: String,
) {
    val platformDependency = dependencies.platform(library(alias))
    dependencies.add(configuration, platformDependency)
}
