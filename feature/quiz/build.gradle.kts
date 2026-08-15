plugins {
    alias(libs.plugins.careerpilot.feature.data)
    alias(libs.plugins.careerpilot.feature.presentation)
    alias(libs.plugins.careerpilot.android.ktor)
    alias(libs.plugins.careerpilot.testing)
}

android {
    namespace = "com.iti.careerpilot.quiz"
}

dependencies {
    // firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.ai)

    implementation(project(":core:common"))
    implementation(project(":core:network"))
    implementation(project(":core:datastore"))
    implementation(project(":core:designsystem"))
}
