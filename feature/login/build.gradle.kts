plugins {
    alias(libs.plugins.careerpilot.feature.data)
    alias(libs.plugins.careerpilot.android.ktor)
    alias(libs.plugins.careerpilot.testing)
}

android {
    namespace = "com.iti.careerpilot.login"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:network"))
}
