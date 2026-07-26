package com.iti.careerpilot.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project

@Suppress("EnumEntryName")
enum class FlavorDimension {
    contentType
}

@Suppress("EnumEntryName")
enum class CareerPilotFlavor(val dimension: FlavorDimension, val applicationIdSuffix: String? = null) {
    prod(FlavorDimension.contentType),
    fake(FlavorDimension.contentType, applicationIdSuffix = ".fake")
}

internal fun Project.configureFlavors() {
    extensions.findByName("android")?.let { androidExtension ->
        when (androidExtension) {
            is ApplicationExtension -> {
                androidExtension.apply {
                    flavorDimensions += FlavorDimension.contentType.name
                    productFlavors {
                        CareerPilotFlavor.entries.forEach {
                            create(it.name) {
                                dimension = it.dimension.name
//                                if (it.applicationIdSuffix != null) {
//                                    applicationIdSuffix = it.applicationIdSuffix
//                                }
                                buildConfigField("String", "FLAVOR_NAME", "\"${it.name}\"")
                            }
                        }
                    }
                }
            }

            is LibraryExtension -> {
                androidExtension.apply {
                    flavorDimensions += FlavorDimension.contentType.name
                    productFlavors {
                        CareerPilotFlavor.entries.forEach {
                            create(it.name) {
                                dimension = it.dimension.name
                                buildConfigField("String", "FLAVOR_NAME", "\"${it.name}\"")
                            }
                        }
                    }
                }
            }
        }
    }
}
