plugins {
    alias(libs.plugins.careerpilot.module.network)
    alias(libs.plugins.careerpilot.testing)
}

android {
    namespace = "com.iti.careerpilot.core.interviews"
}

dependencies {
    api(project(":core:common"))
    implementation(project(":core:network"))
}
