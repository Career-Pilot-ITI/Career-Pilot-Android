plugins {
    alias(libs.plugins.careerpilot.android.library)
    alias(libs.plugins.careerpilot.android.hilt)
}

android {
    namespace = "com.iti.careerpilot.whisper"
}

dependencies {
    implementation(files("libs/sherpa-onnx-1.13.4.aar"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
