plugins {
    alias(libs.plugins.careerpilot.feature.presentation)
}

android {
    namespace = "com.iti.careerpilot.createchallenge"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:network"))
    implementation(project(":core:model"))
    implementation(project(":core:datastore"))
    implementation(project(":core:ai"))

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.ai)
}
