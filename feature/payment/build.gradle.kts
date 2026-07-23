plugins {
    alias(libs.plugins.careerpilot.feature.presentation)
    alias(libs.plugins.careerpilot.android.ktor)
    alias(libs.plugins.careerpilot.kotlin.serialization)
}

android {
    namespace = "com.iti.careerpilot.payment"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:model"))
    implementation(project(":core:network"))
    implementation(project(":core:datastore"))
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}

