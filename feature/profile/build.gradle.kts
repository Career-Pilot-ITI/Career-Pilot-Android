plugins {
    alias(libs.plugins.careerpilot.feature.profile)
}

android {
    namespace = "com.iti.careerpilot.profile"
}

dependencies {
    implementation(project(":core:access"))
    implementation(project(":core:model"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:common"))
    implementation(project(":core:network"))
    implementation(project(":core:datastore"))
}
