plugins {
    alias(libs.plugins.careerpilot.feature.home)
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:common"))
    implementation(project(":core:interviews"))
    implementation(project(":core:network"))
    implementation(project(":core:datastore"))
}
