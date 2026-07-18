plugins {
    alias(libs.plugins.feature.onboarding)
}

android {
    namespace = "com.iti.onboarding"
}

dependencies {
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:network"))
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    testImplementation(libs.junit)
}