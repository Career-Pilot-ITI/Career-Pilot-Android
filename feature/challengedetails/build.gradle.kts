plugins {
    alias(libs.plugins.careerpilot.feature.presentation)
}

android {
    namespace = "com.iti.careerpilot.challengedetails"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:model"))
    implementation(project(":core:challengefirestore"))
}
