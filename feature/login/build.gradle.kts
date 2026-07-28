plugins {
    alias(libs.plugins.careerpilot.feature.data)
    alias(libs.plugins.careerpilot.feature.presentation)
    alias(libs.plugins.careerpilot.android.ktor)
    alias(libs.plugins.careerpilot.testing)
}

android {
    namespace = "com.iti.careerpilot.login"
}

dependencies {
    implementation(project(":core:access"))
    implementation(project(":core:common"))
    implementation(project(":core:network"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:datastore"))

    // country code picker
    implementation(libs.komposecountrycodepicker)
    implementation(libs.androidx.compose.material.icons.core)

    // phone number validation
    implementation(libs.libphonenumber)
}
