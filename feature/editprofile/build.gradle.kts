plugins {
    alias(libs.plugins.careerpilot.feature.profile)
}

android {
    namespace = "com.iti.careerpilot.editprofile"
}

dependencies {
    implementation(libs.androidx.exifinterface)

    implementation(project(":core:designsystem"))
    implementation(project(":core:common"))
    implementation(project(":core:network"))
    implementation(project(":core:datastore"))
}
