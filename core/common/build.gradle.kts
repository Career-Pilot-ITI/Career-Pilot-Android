plugins {
    alias(libs.plugins.careerpilot.android.library)
    alias(libs.plugins.careerpilot.android.compose)
    alias(libs.plugins.careerpilot.android.hilt)
    alias(libs.plugins.careerpilot.testing)
}

android {
    namespace = "com.iti.common"
}

dependencies {
    implementation(libs.androidx.activity.compose)
}
