import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.dagger.hilt.android)
}

val baseUrl: String = run {
    val fromLocalProperties = Properties().run {
        val file = rootProject.file("local.properties")
        if (file.exists()) file.inputStream().use { load(it) }
        getProperty("careerpilot.baseUrl")
    }

    listOf(
        fromLocalProperties,
        providers.gradleProperty("careerpilot.baseUrl").orNull,
        providers.environmentVariable("CAREERPILOT_BASE_URL").orNull,
    ).firstOrNull { !it.isNullOrBlank() }
        ?: throw GradleException(
            """
            Missing backend base URL for :core:network.

            Local development - add this line to local.properties in the project root:

                careerpilot.baseUrl=http://10.0.2.2:8080/
                
            The URL must end with '/'.
            """.trimIndent()
        )
}

android {
    namespace = "com.iti.careerpilot.core.network"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "BASE_URL", "\"$baseUrl\"")
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

}

dependencies {
    // ktor
    api(libs.bundles.ktor)

    // kotlin json
    implementation(libs.kotlinx.serialization.json)

    // hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)

    implementation(project(":core:common"))
}