plugins {
    alias(libs.plugins.careerpilot.feature.onboarding)
}

dependencies {
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:network"))
    implementation(project(":core:model"))
    implementation(project(":core:common"))
}
