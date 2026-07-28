plugins {
    alias(libs.plugins.careerpilot.feature.reports)
}

dependencies {
    implementation(project(":core:access"))
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:network"))
}
