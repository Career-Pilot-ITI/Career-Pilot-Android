plugins {
    alias(libs.plugins.careerpilot.feature.ats)
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:network"))
    implementation(project(":core:model"))
    implementation(project(":core:datastore"))
}
