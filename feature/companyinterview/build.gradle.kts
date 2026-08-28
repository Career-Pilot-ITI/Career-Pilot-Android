plugins {
    alias(libs.plugins.careerpilot.feature.practice.session)
}

android {
    namespace = "com.iti.careerpilot.companyinterview"
}

dependencies {
    implementation(project(":core:access"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:common"))
    implementation(project(":core:network"))
    implementation(project(":core:datastore"))
    implementation(project(":core:whisper"))
    implementation(project(":core:bodylanguage"))
    implementation(project(":core:ai"))
    implementation(project(":core:model"))
    implementation(libs.bundles.camerax)
    implementation(libs.media3.exoplayer)
}
