plugins {
    alias(libs.plugins.careerpilot.android.library)
    alias(libs.plugins.careerpilot.android.hilt)
    alias(libs.plugins.careerpilot.kotlin.serialization)
    alias(libs.plugins.careerpilot.testing)
}

android {
    namespace = "com.iti.careerpilot.challengefirestore"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.ai)
    
    implementation(libs.kotlinx.serialization.json)
}
