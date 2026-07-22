plugins {
    alias(libs.plugins.careerpilot.feature.editprofile)
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:common"))
    implementation(project(":core:network"))
    implementation(project(":core:datastore"))
}
