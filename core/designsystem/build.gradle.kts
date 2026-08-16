plugins {
    alias(libs.plugins.careerpilot.android.library)
    alias(libs.plugins.careerpilot.android.compose)
}

android {
    namespace = "com.iti.careerpilot.core.designsystem"
}
dependencies {
    implementation(project(":core:model"))
    implementation(libs.androidx.compose.googlefonts)
    api(libs.androidx.compose.material3)
    api(libs.androidx.material3.android)
    api(libs.coil.compose)
    api(libs.extended.icons)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.compose.material3)
    implementation(libs.lottie.compose)
    implementation(libs.androidx.activity.compose)
}
