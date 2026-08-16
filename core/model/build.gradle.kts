plugins {
    alias(libs.plugins.careerpilot.android.library)
    alias(libs.plugins.careerpilot.kotlin.serialization)
    alias(libs.plugins.careerpilot.testing)
}

android {
    namespace = "com.iti.core.model"
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
}
