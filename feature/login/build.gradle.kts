plugins {
    alias(libs.plugins.careerpilot.feature.login)
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:network"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:datastore"))
}
