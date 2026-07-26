package com.iti.careerpilot.buildlogic

import org.gradle.api.JavaVersion

internal object CareerPilotBuildConfig {
    const val COMPILE_SDK = 37
    const val TARGET_SDK = 37
    const val DEFAULT_MIN_SDK = 26

    /*
     * Android source and bytecode compatibility remains Java 11.
     * This is independent from the JDK used to run Gradle and AGP.
     */
    val ANDROID_JAVA_VERSION: JavaVersion = JavaVersion.VERSION_11

    const val TEST_RUNNER = "androidx.test.runner.AndroidJUnitRunner"
}
