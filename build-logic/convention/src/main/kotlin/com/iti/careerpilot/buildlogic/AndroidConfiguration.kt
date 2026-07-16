package com.iti.careerpilot.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension

internal fun ApplicationExtension.configureCareerPilotApplication() {
    compileSdk {
        version = release(CareerPilotBuildConfig.COMPILE_SDK)
    }

    defaultConfig {
        minSdk = CareerPilotBuildConfig.DEFAULT_MIN_SDK
        targetSdk = CareerPilotBuildConfig.TARGET_SDK
        testInstrumentationRunner = CareerPilotBuildConfig.TEST_RUNNER
    }

    compileOptions {
        sourceCompatibility = CareerPilotBuildConfig.ANDROID_JAVA_VERSION
        targetCompatibility = CareerPilotBuildConfig.ANDROID_JAVA_VERSION
    }

    buildFeatures {
        buildConfig = true
    }

    packaging {
        resources.excludes += setOf(
            "/META-INF/{AL2.0,LGPL2.1}",
            "META-INF/LICENSE*",
            "META-INF/NOTICE*",
        )
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

internal fun LibraryExtension.configureCareerPilotLibrary() {
    compileSdk {
        version = release(CareerPilotBuildConfig.COMPILE_SDK)
    }

    defaultConfig {
        minSdk = CareerPilotBuildConfig.DEFAULT_MIN_SDK
        testInstrumentationRunner = CareerPilotBuildConfig.TEST_RUNNER
    }

    compileOptions {
        sourceCompatibility = CareerPilotBuildConfig.ANDROID_JAVA_VERSION
        targetCompatibility = CareerPilotBuildConfig.ANDROID_JAVA_VERSION
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

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}
