plugins {
    alias(libs.plugins.careerpilot.feature.reports)
}

android {
    namespace = "com.iti.careerpilot.reports"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:network"))
}
