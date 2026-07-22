package com.iti.careerpilot.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension

private const val FLAVOR_NAME_FIELD = "FLAVOR_NAME"

@Suppress("EnumEntryName")
enum class FlavorDimension {
    contentType,
}

@Suppress("EnumEntryName")
enum class CareerPilotFlavor(
    val dimension: FlavorDimension,
) {
    prod(FlavorDimension.contentType),
    fake(FlavorDimension.contentType)
}

internal fun ApplicationExtension.configureCareerPilotFlavors() {
    flavorDimensions += FlavorDimension.contentType.name
    productFlavors {
        CareerPilotFlavor.entries.forEach { flavor ->
            create(flavor.name) {
                dimension = flavor.dimension.name
                buildConfigField(
                    type = "String",
                    name = FLAVOR_NAME_FIELD,
                    value = "\"${flavor.name}\"",
                )
            }
        }
    }
}

internal fun LibraryExtension.configureCareerPilotFlavors() {
    flavorDimensions += FlavorDimension.contentType.name
    productFlavors {
        CareerPilotFlavor.entries.forEach { flavor ->
            create(flavor.name) {
                dimension = flavor.dimension.name
            }
        }
    }
}
