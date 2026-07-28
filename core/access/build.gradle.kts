plugins {
    alias(libs.plugins.careerpilot.android.library)
    alias(libs.plugins.careerpilot.android.hilt)
    alias(libs.plugins.careerpilot.android.datastore)
    alias(libs.plugins.careerpilot.android.ktor)
    alias(libs.plugins.careerpilot.kotlin.serialization)
    alias(libs.plugins.careerpilot.testing)
}

android {
    namespace = "com.iti.careerpilot.core.access"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:network"))
    implementation(project(":core:datastore"))
    implementation(project(":core:common"))
    implementation(libs.kotlinx.datetime)
}

