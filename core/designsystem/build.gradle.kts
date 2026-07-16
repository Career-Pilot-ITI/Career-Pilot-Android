plugins {
    alias(libs.plugins.careerpilot.android.library)
    alias(libs.plugins.careerpilot.android.compose)
}

android {
    namespace = "com.iti.careerpilot.core.designsystem"
}

dependencies {
    implementation(libs.androidx.compose.googlefonts)
}
