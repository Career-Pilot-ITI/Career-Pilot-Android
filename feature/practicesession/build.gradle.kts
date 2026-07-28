plugins {
    alias(libs.plugins.careerpilot.feature.practice.session)
}

android {
    namespace = "com.iti.careerpilot.practicesession"
}

dependencies {
    implementation(project(":core:access"))
    implementation(libs.media3.exoplayer)
    implementation(project(":core:designsystem"))
    implementation(project(":core:common"))
    implementation(project(":core:network"))
    implementation(project(":core:datastore"))
    implementation(project(":core:whisper"))
}
