plugins {
    alias(libs.plugins.careerpilot.feature.presentation)
}

android {
    namespace = "com.iti.careerpilot.challengedashboard"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))
}
