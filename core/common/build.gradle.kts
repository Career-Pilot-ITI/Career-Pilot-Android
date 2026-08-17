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
    implementation(project(":core:model"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.ai)
    implementation(libs.zxing.core)
    testImplementation(libs.robolectric)
}
