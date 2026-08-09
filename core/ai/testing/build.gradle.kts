plugins {
    alias(libs.plugins.careerpilot.android.library)
}

android {
    namespace = "com.iti.careerpilot.ai.testing"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:ai"))
    implementation(libs.kotlinx.coroutines.core)
}
