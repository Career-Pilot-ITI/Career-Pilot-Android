plugins {
    alias(libs.plugins.careerpilot.feature.home)
}

android {
    namespace = "com.iti.careerpilot.home"
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:common"))
    implementation(project(":core:network"))
}
