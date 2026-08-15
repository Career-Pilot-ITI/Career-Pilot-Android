plugins {
    alias(libs.plugins.careerpilot.feature.presentation)
}

android {
    namespace = "com.iti.careerpilot.challenges"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))
}
