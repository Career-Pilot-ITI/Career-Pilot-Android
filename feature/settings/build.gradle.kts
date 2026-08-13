plugins {
    alias(libs.plugins.careerpilot.feature.settings)
}

android {
    namespace = "com.iti.careerpilot.settings"
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:common"))
    implementation(project(":core:datastore"))
}