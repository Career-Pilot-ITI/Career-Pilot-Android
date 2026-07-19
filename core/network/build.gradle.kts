import java.util.Properties

plugins {
    alias(libs.plugins.careerpilot.module.network)
    alias(libs.plugins.careerpilot.testing)
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

    defaultConfig {
        buildConfigField("String", "BASE_URL", "\"$baseUrl\"")
    }

    buildFeatures {
        buildConfig = true
    }
}
dependencies{
    implementation(project(":core:common"))
}
